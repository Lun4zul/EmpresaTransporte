const express = require('express');
const Database = require('better-sqlite3');
const path = require('path');

const app = express();
const PORT = 3000;

// Conectar a la base de datos SQLite existente (tienda.db)
const dbPath = path.join(__dirname, 'tienda.db');
const db = new Database(dbPath);

// Inicializar tablas si no existen
db.exec(`
  CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario TEXT UNIQUE,
    clave TEXT,
    rol TEXT DEFAULT 'pasajero'
  );
  CREATE TABLE IF NOT EXISTS productos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT,
    precio REAL
  );
  CREATE TABLE IF NOT EXISTS compras (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario TEXT,
    producto TEXT,
    cantidad INTEGER
  );
  CREATE TABLE IF NOT EXISTS solicitudes_cancelacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    radicado TEXT UNIQUE,
    usuario TEXT,
    compra_id INTEGER,
    ruta TEXT,
    motivo TEXT,
    contacto TEXT,
    estado TEXT DEFAULT 'PENDIENTE',
    fecha TEXT,
    respuesta_admin TEXT
  );
`);

// Asegurar existencia de columna 'rol' en tabla usuarios
try {
  db.exec("ALTER TABLE usuarios ADD COLUMN rol TEXT DEFAULT 'pasajero'");
} catch (e) {
  // Columna ya existe
}

// Inicializar usuarios semilla (Superadmin, Administrador creado y Pasajero de prueba)
db.prepare("INSERT OR IGNORE INTO usuarios(usuario, clave, rol) VALUES(?, ?, ?)").run('superadmin', 'admin123', 'superadmin');
db.prepare("INSERT OR IGNORE INTO usuarios(usuario, clave, rol) VALUES(?, ?, ?)").run('admin', 'admin123', 'admin');
db.prepare("INSERT OR IGNORE INTO usuarios(usuario, clave, rol) VALUES(?, ?, ?)").run('cliente1', '1234', 'pasajero');

// Asegurar que el pasajero Andrea continúe eliminado
db.prepare("DELETE FROM usuarios WHERE usuario = 'Andrea'").run();
db.prepare("DELETE FROM compras WHERE usuario = 'Andrea'").run();
db.prepare("DELETE FROM solicitudes_cancelacion WHERE usuario = 'Andrea'").run();

// Catálogo de rutas inicial si está vacío
const totalProductos = db.prepare('SELECT COUNT(*) as c FROM productos').get().c;
if (totalProductos === 0) {
  const rutas = [
    ['Pasaje Bogotá - Medellín (Servicio 2G)', 85000],
    ['Pasaje Bogotá - Cali (Servicio DuoBus)', 95000],
    ['Pasaje Bogotá - Bucaramanga (Servicio Royal)', 78000],
    ['Pasaje Bogotá - Pereira (Servicio 2G Gold)', 70000],
    ['Pasaje Bogotá - Barranquilla (Servicio VIP)', 135000],
    ['Pasaje Bogotá - Ibagué (Servicio Plus)', 45000],
    ['Pasaje Medellín - Cartagena (Servicio Costa)', 140000],
  ];
  const insertRuta = db.prepare('INSERT INTO productos(nombre, precio) VALUES(?, ?)');
  rutas.forEach(r => insertRuta.run(r[0], r[1]));
}

// Middlewares
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Middleware de verificación de roles para API REST (RBAC)
function verificarRol(rolesPermitidos = []) {
  return (req, res, next) => {
    const userRole = req.headers['x-user-role'] || req.query.rol;
    if (!userRole) {
      return res.status(401).json({ exito: false, mensaje: 'Cabecera de autenticación x-user-role requerida.' });
    }
    // El superadministrador cuenta con permisos globales en todas las rutas administrativas
    if (userRole === 'superadmin') {
      return next();
    }
    if (!rolesPermitidos.includes(userRole)) {
      return res.status(403).json({
        exito: false,
        mensaje: `Acceso denegado: El recurso requiere rol [${rolesPermitidos.join(', ')}]. Tu rol actual es [${userRole}].`
      });
    }
    next();
  };
}

// =========================================================
// API REST — Autenticación y Login (AuthService)
// =========================================================
// Pasajeros, Administradores y Superadministrador SÍ pueden iniciar sesión
app.post('/api/v1/auth/login', (req, res) => {
  const { usuario, clave } = req.body;
  if (!usuario || !clave) return res.status(400).json({ exito: false, mensaje: 'Datos incompletos.' });

  const row = db.prepare('SELECT id, usuario, clave, rol FROM usuarios WHERE usuario = ? AND clave = ?').get(usuario, clave);
  if (!row) {
    return res.status(401).json({ exito: false, mensaje: 'Usuario o contraseña incorrectos.' });
  }

  const rol = row.rol || 'pasajero';
  res.json({
    exito: true,
    mensaje: `Bienvenido al sistema, ${row.usuario}`,
    usuario: row.usuario,
    rol,
    token: `JWT-BOLIVARIANO-${rol.toUpperCase()}-${Date.now()}`
  });
});

// Registro público: EXCLUSIVAMENTE para Pasajeros
// Los administradores no se pueden registrar aquí; solo los registra el Superadministrador
app.post('/api/v1/auth/register', (req, res) => {
  const { usuario, clave } = req.body;
  if (!usuario || !clave) return res.status(400).json({ exito: false, mensaje: 'Complete todos los campos.' });

  try {
    db.prepare("INSERT INTO usuarios(usuario, clave, rol) VALUES(?, ?, 'pasajero')").run(usuario, clave);
    res.json({
      exito: true,
      mensaje: '¡Cuenta de Pasajero creada exitosamente! Ya puede iniciar sesión con sus credenciales.',
      usuario,
      rol: 'pasajero'
    });
  } catch (e) {
    res.status(400).json({ exito: false, mensaje: 'El nombre de usuario ya existe en el sistema.' });
  }
});

// =========================================================
// API REST — Gestión Exclusiva de Superadmin (Registrar Administradores)
// =========================================================
// POST: Solo el Superadministrador puede registrar administradores y asignarles su contraseña
app.post('/api/v1/superadmin/crear-administrador', verificarRol(['superadmin']), (req, res) => {
  const { usuario, clave } = req.body;
  if (!usuario || !clave) {
    return res.status(400).json({ exito: false, mensaje: 'Debe ingresar el nombre de usuario y la contraseña para el nuevo administrador.' });
  }

  try {
    db.prepare("INSERT INTO usuarios(usuario, clave, rol) VALUES(?, ?, 'admin')").run(usuario, clave);
    res.json({
      exito: true,
      mensaje: `Usuario Administrador '${usuario}' registrado exitosamente por el Superadministrador con su contraseña asignada.`,
      usuario,
      rol: 'admin'
    });
  } catch (e) {
    res.status(400).json({ exito: false, mensaje: 'El nombre de usuario ya existe en el sistema.' });
  }
});

// GET: Listar administradores registrados (Exclusivo Superadmin)
app.get('/api/v1/superadmin/listar-administradores', verificarRol(['superadmin']), (req, res) => {
  const administradores = db.prepare("SELECT id, usuario, rol FROM usuarios WHERE rol = 'admin' ORDER BY id DESC").all();
  res.json(administradores);
});

// =========================================================
// API REST — Catálogo de Rutas (CatalogService)
// =========================================================
// GET: Abierto a Pasajeros, Administradores y Superadmin
app.get('/api/v1/catalogo/rutas', (req, res) => {
  const rutas = db.prepare('SELECT id, nombre, precio FROM productos ORDER BY id ASC').all();
  res.json(rutas);
});

// POST: Administradores y Superadmin pueden agregar rutas
app.post('/api/v1/catalogo/rutas', verificarRol(['admin', 'superadmin']), (req, res) => {
  const { nombre, precio } = req.body;
  if (!nombre || !precio) return res.status(400).json({ exito: false, mensaje: 'Nombre y precio son requeridos.' });
  db.prepare('INSERT INTO productos(nombre, precio) VALUES(?, ?)').run(nombre, parseFloat(precio));
  res.json({ exito: true, mensaje: 'Ruta registrada en el catálogo exitosamente.' });
});

// DELETE: Administradores y Superadmin pueden eliminar rutas
app.delete('/api/v1/catalogo/rutas/:id', verificarRol(['admin', 'superadmin']), (req, res) => {
  const { id } = req.params;
  const result = db.prepare('DELETE FROM productos WHERE id = ?').run(parseInt(id));
  if (result.changes > 0) {
    res.json({ exito: true, mensaje: 'Ruta eliminada del catálogo.' });
  } else {
    res.status(404).json({ exito: false, mensaje: 'Ruta no encontrada.' });
  }
});

// =========================================================
// API REST — Ventas y Compras (BookingService)
// =========================================================
// POST: Crear compra de pasaje
app.post('/api/v1/ventas/crear-orden', (req, res) => {
  const { usuario, producto, cantidad } = req.body;
  if (!usuario || !producto || !cantidad) return res.status(400).json({ exito: false, mensaje: 'Datos incompletos.' });
  try {
    const info = db.prepare('INSERT INTO compras(usuario, producto, cantidad) VALUES(?, ?, ?)').run(usuario, producto, parseInt(cantidad));
    const tiqueteId = 'TQK-' + Date.now().toString().slice(-6);
    res.json({
      exito: true,
      mensaje: '¡Compra registrada exitosamente!',
      compraId: info.lastInsertRowid,
      tiqueteId,
      usuario,
      producto,
      cantidad
    });
  } catch (e) {
    res.status(500).json({ exito: false, mensaje: 'Error al registrar la compra.' });
  }
});

// GET: Pasajero ve ÚNICAMENTE sus compras; Administradores y Superadmin ven todas o filtran
app.get('/api/v1/ventas/historial', (req, res) => {
  const userRole = req.headers['x-user-role'];
  const userName = req.headers['x-user-name'] || req.query.usuario;

  if (userRole === 'pasajero') {
    const targetUser = userName || req.query.usuario;
    const compras = db.prepare('SELECT id, usuario, producto, cantidad FROM compras WHERE usuario = ? ORDER BY id DESC').all(targetUser);
    return res.json(compras);
  }

  // Administrador y Superadministrador
  const { usuario } = req.query;
  let compras;
  if (usuario) {
    compras = db.prepare('SELECT id, usuario, producto, cantidad FROM compras WHERE usuario = ? ORDER BY id DESC').all(usuario);
  } else {
    compras = db.prepare('SELECT id, usuario, producto, cantidad FROM compras ORDER BY id DESC').all();
  }
  res.json(compras);
});

// DELETE: Administradores y Superadmin pueden eliminar compras directamente
app.delete('/api/v1/ventas/compras/:id', verificarRol(['admin', 'superadmin']), (req, res) => {
  const { id } = req.params;
  const result = db.prepare('DELETE FROM compras WHERE id = ?').run(parseInt(id));
  if (result.changes > 0) {
    res.json({ exito: true, mensaje: 'Pasaje eliminado exitosamente del sistema.' });
  } else {
    res.status(404).json({ exito: false, mensaje: 'Pasaje no encontrado.' });
  }
});

// =========================================================
// API REST — Servicio al Cliente / Solicitudes de Cancelación
// =========================================================
// POST: Pasajero radica solicitud para cancelar viaje
app.post('/api/v1/servicio-cliente/solicitar-cancelacion', (req, res) => {
  const { usuario, compraId, motivo, contacto } = req.body;
  if (!usuario || !compraId || !motivo) {
    return res.status(400).json({ exito: false, mensaje: 'Faltan datos obligatorios para radicar la solicitud.' });
  }

  const compra = db.prepare('SELECT * FROM compras WHERE id = ?').get(parseInt(compraId));
  if (!compra) {
    return res.status(404).json({ exito: false, mensaje: 'El pasaje o viaje especificado no existe.' });
  }

  const userRole = req.headers['x-user-role'];
  if (userRole === 'pasajero' && compra.usuario !== usuario) {
    return res.status(403).json({ exito: false, mensaje: 'No tiene permiso para solicitar la cancelación de un pasaje ajeno.' });
  }

  const radicado = 'PQRS-' + Date.now().toString().slice(-6);
  const fecha = new Date().toLocaleString('es-CO');

  db.prepare(`
    INSERT INTO solicitudes_cancelacion(radicado, usuario, compra_id, ruta, motivo, contacto, estado, fecha, respuesta_admin)
    VALUES(?, ?, ?, ?, ?, ?, 'PENDIENTE', ?, 'En revisión por el equipo de Servicio al Cliente')
  `).run(radicado, usuario, compra.id, compra.producto, motivo, contacto || 'No registrado', fecha);

  res.json({
    exito: true,
    mensaje: `Solicitud radicada con éxito. El equipo de Servicio al Cliente gestionará la cancelación del viaje.`,
    radicado,
    estado: 'PENDIENTE'
  });
});

// GET: Pasajero consulta sus propias solicitudes
app.get('/api/v1/servicio-cliente/mis-solicitudes', (req, res) => {
  const usuario = req.headers['x-user-name'] || req.query.usuario;
  if (!usuario) return res.status(400).json({ exito: false, mensaje: 'Usuario no especificado.' });
  const solicitudes = db.prepare('SELECT * FROM solicitudes_cancelacion WHERE usuario = ? ORDER BY id DESC').all(usuario);
  res.json(solicitudes);
});

// GET: Administrador y Superadmin (ver todas las solicitudes de cancelación y contacto)
app.get('/api/v1/servicio-cliente/solicitudes', verificarRol(['admin', 'superadmin']), (req, res) => {
  const solicitudes = db.prepare('SELECT * FROM solicitudes_cancelacion ORDER BY id DESC').all();
  res.json(solicitudes);
});

// POST: Administrador y Superadmin (Gestionar / Aprobar eliminación de viaje o rechazar)
app.post('/api/v1/servicio-cliente/gestionar-solicitud', verificarRol(['admin', 'superadmin']), (req, res) => {
  const { solicitudId, accion, respuesta } = req.body;
  const solicitud = db.prepare('SELECT * FROM solicitudes_cancelacion WHERE id = ?').get(parseInt(solicitudId));
  if (!solicitud) return res.status(404).json({ exito: false, mensaje: 'Solicitud no encontrada.' });

  if (accion === 'APROBAR') {
    const deleteOp = db.prepare('DELETE FROM compras WHERE id = ?').run(solicitud.compra_id);
    const notaAdmin = respuesta || 'Viaje cancelado y eliminado de la base de datos tras contacto con el cliente.';
    db.prepare(`
      UPDATE solicitudes_cancelacion
      SET estado = 'APROBADA - VIAJE ELIMINADO', respuesta_admin = ?
      WHERE id = ?
    `).run(notaAdmin, solicitud.id);

    res.json({
      exito: true,
      mensaje: `Solicitud ${solicitud.radicado} APROBADA. El viaje ID #${solicitud.compra_id} fue eliminado de la base de datos.`,
      compraEliminada: deleteOp.changes > 0
    });
  } else {
    const notaAdmin = respuesta || 'Solicitud no aprobada según políticas de transporte intermunicipal.';
    db.prepare(`
      UPDATE solicitudes_cancelacion
      SET estado = 'RECHAZADA', respuesta_admin = ?
      WHERE id = ?
    `).run(notaAdmin, solicitud.id);

    res.json({
      exito: true,
      mensaje: `Solicitud ${solicitud.radicado} fue RECHAZADA.`
    });
  }
});

// =========================================================
// API REST — Planilla de Despacho (DispatchService)
// =========================================================
// GET: Administrador y Superadmin
app.get('/api/v1/despachos/planilla', verificarRol(['admin', 'superadmin']), (req, res) => {
  const compras = db.prepare('SELECT id, usuario, producto, cantidad FROM compras ORDER BY id DESC').all();
  const totalPasajes = compras.reduce((acc, c) => acc + c.cantidad, 0);
  res.json({
    empresa: 'Expreso Bolivariano S.A.',
    planillaId: 'PLN-2026-0928',
    busNumero: 'MÓVIL-4052',
    conductor: 'Carlos Alberto Mendoza',
    ruta: 'Bogotá Terminal Salitre → Medellín Terminal Norte',
    totalTransacciones: compras.length,
    totalPasajes,
    pasajeros: compras,
    estado: 'ACTIVA',
  });
});

// Iniciar servidor
app.listen(PORT, () => {
  console.log(`\n╔══════════════════════════════════════════════════════════════╗`);
  console.log(`║   EXPRESO BOLIVARIANO — Servidor Web SOA activo              ║`);
  console.log(`║   http://localhost:${PORT}                                       ║`);
  console.log(`║   Control de Acceso por Roles (RBAC):                        ║`);
  console.log(`║     - Pasajero: Registro público en app, compra, PQRS        ║`);
  console.log(`║     - Administrador: Inicia sesión (Catálogo, Despacho, PQRS)║`);
  console.log(`║     - Superadmin: Control total + Registra Administradores   ║`);
  console.log(`║   (Administradores NO pueden autoregistrarse en la app)      ║`);
  console.log(`╚══════════════════════════════════════════════════════════════╝\n`);
});
