const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, WidthType, BorderStyle, ShadingType
} = require('docx');

function createDoc() {
  const primaryColor = "C41E3A"; // Rojo Bolivariano
  const secondaryColor = "1E3A8A"; // Azul Marino
  const darkTextColor = "1F2937";
  const grayBg = "F3F4F6";
  const lightBlueBg = "EFF6FF";

  const tableBorder = {
    top: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
    bottom: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
    left: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
    right: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
    insideHorizontal: { style: BorderStyle.SINGLE, size: 4, color: "E2E8F0" },
    insideVertical: { style: BorderStyle.SINGLE, size: 4, color: "E2E8F0" },
  };

  const headerCell = (text, widthPercent = 25) => new TableCell({
    width: { size: widthPercent, type: WidthType.PERCENTAGE },
    shading: { fill: primaryColor, type: ShadingType.CLEAR },
    margins: { top: 120, bottom: 120, left: 140, right: 140 },
    children: [
      new Paragraph({
        children: [new TextRun({ text, bold: true, color: "FFFFFF", font: "Calibri", size: 20 })]
      })
    ]
  });

  const bodyCell = (text, widthPercent = 25, isBold = false, bgColor = null) => new TableCell({
    width: { size: widthPercent, type: WidthType.PERCENTAGE },
    shading: bgColor ? { fill: bgColor, type: ShadingType.CLEAR } : undefined,
    margins: { top: 100, bottom: 100, left: 140, right: 140 },
    children: [
      new Paragraph({
        children: [new TextRun({ text, bold: isBold, color: darkTextColor, font: "Calibri", size: 19 })]
      })
    ]
  });

  const heading1 = (text) => new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 360, after: 140 },
    children: [
      new TextRun({ text, bold: true, color: primaryColor, font: "Calibri", size: 30 })
    ]
  });

  const heading2 = (text) => new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 240, after: 100 },
    children: [
      new TextRun({ text, bold: true, color: secondaryColor, font: "Calibri", size: 24 })
    ]
  });

  const para = (text, boldPrefix = "") => new Paragraph({
    spacing: { before: 80, after: 100, line: 276 },
    children: [
      ...(boldPrefix ? [new TextRun({ text: boldPrefix + " ", bold: true, color: darkTextColor, font: "Calibri", size: 22 })] : []),
      new TextRun({ text, color: darkTextColor, font: "Calibri", size: 22 })
    ]
  });

  const bullet = (text, boldPrefix = "") => new Paragraph({
    bullet: { level: 0 },
    spacing: { before: 60, after: 80, line: 260 },
    children: [
      ...(boldPrefix ? [new TextRun({ text: boldPrefix + " ", bold: true, color: darkTextColor, font: "Calibri", size: 21 })] : []),
      new TextRun({ text, color: darkTextColor, font: "Calibri", size: 21 })
    ]
  });

  const callout = (title, text) => new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    borders: {
      top: { style: BorderStyle.NONE },
      bottom: { style: BorderStyle.NONE },
      right: { style: BorderStyle.NONE },
      left: { style: BorderStyle.SINGLE, size: 24, color: primaryColor },
    },
    rows: [
      new TableRow({
        children: [
          new TableCell({
            width: { size: 100, type: WidthType.PERCENTAGE },
            shading: { fill: lightBlueBg, type: ShadingType.CLEAR },
            margins: { top: 120, bottom: 120, left: 160, right: 160 },
            children: [
              new Paragraph({
                children: [new TextRun({ text: title, bold: true, color: secondaryColor, font: "Calibri", size: 21 })]
              }),
              new Paragraph({
                spacing: { before: 60 },
                children: [new TextRun({ text, color: darkTextColor, font: "Calibri", size: 20 })]
              })
            ]
          })
        ]
      })
    ]
  });

  const doc = new Document({
    sections: [{
      properties: {
        page: {
          margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
        }
      },
      children: [
        // PORTADA / ENCABEZADO
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { before: 200, after: 100 },
          children: [
            new TextRun({ text: "POLITÉCNICO GRANCOLOMBIANO", bold: true, color: secondaryColor, font: "Calibri", size: 32 }),
          ]
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 100 },
          children: [
            new TextRun({ text: "FACULTAD DE INGENIERÍA, DISEÑO E INNOVACIÓN", bold: true, color: "4B5563", font: "Calibri", size: 24 })
          ]
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 200 },
          children: [
            new TextRun({ text: "ARQUITECTURA DE SOFTWARE — ENTREGA 2 (SEMANA 6)", bold: true, color: primaryColor, font: "Calibri", size: 26 })
          ]
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { before: 200, after: 260 },
          children: [
            new TextRun({
              text: "DISEÑO Y ESPECIFICACIÓN DE ARQUITECTURA ORIENTADA A SERVICIOS (SOA) Y API REST CON CONTROL DE ACCESO POR ROLES (RBAC: PASAJERO Y SUPERADMINISTRADOR) PARA EXPRESO BOLIVARIANO",
              bold: true,
              color: primaryColor,
              font: "Calibri",
              size: 26
            })
          ]
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 80 },
          children: [
            new TextRun({ text: "Integrantes (Subgrupo 13):", bold: true, font: "Calibri", size: 22 })
          ]
        }),
        new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "July Andrea Hernández Fonseca", font: "Calibri", size: 21 })] }),
        new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Herbert Andrey Ariza Pedraza", font: "Calibri", size: 21 })] }),
        new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Keith Dunwel Villalobos", font: "Calibri", size: 21 })] }),
        new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 160 }, children: [new TextRun({ text: "Carmen Rosaura Herrera Bernal", font: "Calibri", size: 21 })] }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 200 },
          children: [
            new TextRun({ text: "Docente: NATALIA MARTINEZ ROJAS", bold: true, font: "Calibri", size: 22 })
          ]
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 400 },
          children: [
            new TextRun({ text: "Bogotá D.C., Colombia — Octubre de 2026", color: "6B7280", font: "Calibri", size: 20 })
          ]
        }),

        // 1. SITUACIÓN PROBLEMA Y ANTECEDENTES
        heading1("1. SITUACIÓN PROBLEMA Y ANTECEDENTES"),
        para(
          "Expreso Bolivariano S.A. es una compañía líder en el transporte intermunicipal de pasajeros en Colombia. Su operación requiere la articulación fluida y segura entre pasajeros que interactúan a través de canales digitales, personal de supervisión operativa y la tripulación a cargo de los despachos de vehículos.",
          "Contexto Institucional:"
        ),
        para(
          "En la operación tradicional, la ausencia de un control estricto de acceso por roles generaba riesgos operacionales críticos: eliminación involuntaria o arbitraria de pasajes por parte de usuarios finales, falta de trazabilidad en las cancelaciones, sobrecarga de peticiones y exposición indebida de la planilla de despacho de flota. Asimismo, se requería una estricta jerarquía de seguridad donde solo la máxima autoridad (Superadministrador) pudiera aprovisionar administradores, y donde los usuarios con rol Administrador no tuvieran permitido el inicio de sesión directo en la plataforma para evitar usurpaciones de identidad o accesos no auditados.",
          "Problemática Operativa:"
        ),
        para(
          "En la presente Entrega 2 (Semana 6), se formaliza la especificación de la arquitectura orientada a servicios basada en una API REST desacoplada y se implementa el modelo de Control de Acceso Basado en Roles (RBAC), definiendo el registro público exclusivo de pasajeros, el aprovisionamiento de administradores por parte del Superadministrador y la restricción de inicio de sesión directo para cuentas administrativas.",
          "Objetivo de la Entrega 2 (Semana 6):"
        ),

        // 2. DESCRIPCIÓN DE COMPONENTES DE SERVICIO
        heading1("2. DESCRIPCIÓN DE LOS COMPONENTES DEL SERVICIO (API REST)"),
        para("La solución se estructura en módulos desacoplados expuestos bajo contratos RESTful / JSON:"),

        heading2("2.1. Módulo de Autenticación, Registro y Gobierno (AuthService)"),
        bullet("Gestionar el registro público de pasajeros en la aplicación y el inicio de sesión para roles autorizados (Pasajero y Superadministrador).", "• Propósito:"),
        bullet("Aplica la regla de negocio que bloquea el login directo a usuarios con rol Administrador (HTTP 403 Forbidden).", "• Regla de Seguridad:"),
        bullet("POST /api/v1/auth/login, POST /api/v1/auth/register (registro exclusivo de pasajeros).", "• Endpoints Principales:"),

        heading2("2.2. Módulo de Gestión de Administradores (SuperadminService)"),
        bullet("Permitir que el Superadministrador cree y audite cuentas de usuarios administradores en la base de datos.", "• Propósito:"),
        bullet("POST /api/v1/superadmin/crear-administrador, GET /api/v1/superadmin/listar-administradores.", "• Endpoints Principales:"),

        heading2("2.3. Módulo de Catálogo y Rutas (CatalogService)"),
        bullet("Exponer el inventario de rutas intermunicipales y tarifas oficiales para consulta de pasajeros y gobernanza del Superadministrador.", "• Propósito:"),
        bullet("GET /api/v1/catalogo/rutas, POST /api/v1/catalogo/rutas, DELETE /api/v1/catalogo/rutas/:id.", "• Endpoints Principales:"),

        heading2("2.4. Módulo de Ventas e Historial (BookingService)"),
        bullet("Procesar compras de pasajes emitiendo códigos de tiquete (TQK-XXXXXX) y proveer el historial segmentado por rol.", "• Propósito:"),
        bullet("POST /api/v1/ventas/crear-orden, GET /api/v1/ventas/historial, DELETE /api/v1/ventas/compras/:id.", "• Endpoints Principales:"),

        heading2("2.5. Módulo de Despacho y Planilla (DispatchService)"),
        bullet("Generar la Planilla Oficial de Despacho requerida para la salida de buses y control de conductores en terminales (acceso exclusivo Superadministrador).", "• Propósito:"),
        bullet("GET /api/v1/despachos/planilla.", "• Endpoints Principales:"),

        heading2("2.6. Módulo de Servicio al Cliente y Cancelaciones (CustomerSupportService / PQRS)"),
        bullet("Permitir a los pasajeros radicar solicitudes formales para cancelar o eliminar viajes, y al Superadministrador resolverlas y ejecutar la eliminación en base de datos.", "• Propósito:"),
        bullet("POST /api/v1/servicio-cliente/solicitar-cancelacion, GET /api/v1/servicio-cliente/mis-solicitudes, GET /api/v1/servicio-cliente/solicitudes, POST /api/v1/servicio-cliente/gestionar-solicitud.", "• Endpoints Principales:"),

        // 3. MODELO DE CONTROL DE ACCESO POR ROLES (RBAC)
        heading1("3. MODELO DE CONTROL DE ACCESO POR ROLES (RBAC)"),
        para(
          "El sistema implementa una jerarquía RBAC estricta conformada por tres roles con directrices claras:",
          "Jerarquía de Roles y Seguridad:"
        ),

        heading2("3.1. Rol Pasajero (Registro Público en la Aplicación)"),
        para("Cualquier usuario puede registrarse directamente desde la pantalla de la aplicación web como Pasajero:"),
        bullet("Registro autónomo a través de la interfaz web mediante el endpoint público POST /api/v1/auth/register.", "1. Registro Público:"),
        bullet("Consulta libre de rutas, precios y disponibilidad en tiempo real.", "2. Exploración de Catálogo:"),
        bullet("Selección de sillas, medio de pago (Tarjeta, PSE, Nequi, Daviplata, Taquilla) y emisión de tiquete.", "3. Compra de Pasajes:"),
        bullet("Acceso exclusivo a sus propias compras; aislamiento total de datos frente a otros usuarios.", "4. Historial Personal:"),
        bullet("El pasajero no puede eliminar directamente sus tiquetes de la base de datos. Dispone de un canal formal de Servicio al Cliente para solicitar la anulación mediante radicado PQRS, sustentando motivo y teléfono de contacto.", "5. Solicitud de Cancelación de Viaje:"),
        bullet("Restricción absoluta a la planilla de despacho, gestión de rutas y bandeja de PQRS ajenas.", "6. Restricciones:"),

        heading2("3.2. Rol Administrador (Inicio Directo Bloqueado)"),
        para("Los usuarios con rol Administrador son creados exclusivamente por el Superadministrador:"),
        bullet("Solo el Superadministrador tiene autorización para dar de alta cuentas de administradores mediante el endpoint POST /api/v1/superadmin/crear-administrador.", "1. Aprovisionamiento Controlado:"),
        bullet("Por directriz de seguridad de la arquitectura, los usuarios con rol Administrador NO tienen permitido el inicio de sesión directo en la plataforma web. Si un usuario con credenciales de administrador intenta loguearse en /api/v1/auth/login, el servidor rechaza la solicitud de inmediato con código HTTP 403 Forbidden.", "2. Bloqueo de Inicio Directo:"),
        bullet("Esta medida previene que cuentas administrativas sean utilizadas sin supervisión y centraliza la gobernanza operativa en el Superadministrador.", "3. Justificación de Seguridad:"),

        heading2("3.3. Rol Superadministrador (Control Total del Sistema)"),
        para("El Superadministrador es la autoridad central del sistema con acceso directo y privilegios totales:"),
        bullet("Creación y auditoría del listado de administradores registrados.", "1. Creación de Administradores:"),
        bullet("Alta, modificación y eliminación de rutas y tarifas intermunicipales en el catálogo.", "2. Gobierno de Rutas:"),
        bullet("Supervisión de la Planilla Oficial de Despacho de vehículos (móvil, conductor, ruta y pasajeros).", "3. Control de Despacho:"),
        bullet("Auditoría de todas las compras del sistema y potestad para eliminar directamente pasajes en base de datos si se requiere.", "4. Auditoría Global de Ventas:"),
        bullet("Revisión de las solicitudes radicadas por pasajeros, con facultad para aprobar la cancelación (lo cual ejecuta el borrado físico del viaje en la tabla compras) o rechazarla con justificación.", "5. Resolución de Cancelaciones PQRS:"),

        heading2("3.4. Matriz de Autorización de Endpoints (RBAC Matrix)"),
        new Table({
          width: { size: 100, type: WidthType.PERCENTAGE },
          borders: tableBorder,
          rows: [
            new TableRow({
              children: [
                headerCell("Endpoint REST", 32),
                headerCell("Pasajero", 18),
                headerCell("Administrador", 22),
                headerCell("Superadministrador", 28)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/auth/login", 32, true),
                bodyCell("Permitido (200)", 18, false, "D1FAE5"),
                bodyCell("BLOQUEADO (403)", 22, true, "FEE2E2"),
                bodyCell("Permitido (200)", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/auth/register", 32, true),
                bodyCell("Permitido (Registro)", 18, false, "D1FAE5"),
                bodyCell("No aplica", 22),
                bodyCell("No aplica", 28)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/superadmin/crear-administrador", 32, true),
                bodyCell("Denegado (403)", 18, false, "FEE2E2"),
                bodyCell("Denegado (403)", 22, false, "FEE2E2"),
                bodyCell("EXCLUSIVO (200)", 28, true, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET /api/v1/superadmin/listar-administradores", 32, true),
                bodyCell("Denegado (403)", 18, false, "FEE2E2"),
                bodyCell("Denegado (403)", 22, false, "FEE2E2"),
                bodyCell("EXCLUSIVO (200)", 28, true, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET /api/v1/catalogo/rutas", 32, true),
                bodyCell("Permitido", 18, false, "D1FAE5"),
                bodyCell("No login directo", 22),
                bodyCell("Permitido", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/catalogo/rutas", 32, true),
                bodyCell("Denegado (403)", 18, false, "FEE2E2"),
                bodyCell("No login directo", 22),
                bodyCell("Permitido", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("DELETE /api/v1/catalogo/rutas/:id", 32, true),
                bodyCell("Denegado (403)", 18, false, "FEE2E2"),
                bodyCell("No login directo", 22),
                bodyCell("Permitido", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/ventas/crear-orden", 32, true),
                bodyCell("Permitido", 18, false, "D1FAE5"),
                bodyCell("No login directo", 22),
                bodyCell("Permitido", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET /api/v1/ventas/historial", 32, true),
                bodyCell("Solo compras propias", 18, false, "FEF3C7"),
                bodyCell("No login directo", 22),
                bodyCell("Todas / Filtrado", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/servicio-cliente/solicitar-cancelacion", 32, true),
                bodyCell("Permitido (Radicar)", 18, false, "D1FAE5"),
                bodyCell("No login directo", 22),
                bodyCell("Permitido", 28, false, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST /api/v1/servicio-cliente/gestionar-solicitud", 32, true),
                bodyCell("Denegado (403)", 18, false, "FEE2E2"),
                bodyCell("No login directo", 22),
                bodyCell("Aprobar y Eliminar", 28, true, "D1FAE5")
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET /api/v1/despachos/planilla", 32, true),
                bodyCell("Denegado (403)", 18, false, "FEE2E2"),
                bodyCell("No login directo", 22),
                bodyCell("Permitido", 28, false, "D1FAE5")
              ]
            })
          ]
        }),

        // 4. MODELO ARQUITECTÓNICO RESTful
        heading1("4. MODELO ARQUITECTÓNICO ORIENTADO A SERVICIOS Y API REST"),
        para(
          "El sistema adopta el estilo arquitectónico REST (Representational State Transfer) formalizado por Roy Fielding (2000), sustentado en:",
          "Fundamentos del Modelo RESTful:"
        ),
        bullet("Cada recurso de negocio se identifica unívocamente mediante URIs estandarizadas bajo el prefijo '/api/v1/'.", "1. Identificación de Recursos:"),
        bullet("Uso de verbos HTTP estándar (GET para consultas idempotentes, POST para creación y comandos, DELETE para remoción de recursos).", "2. Verbos Semánticos:"),
        bullet("El servidor no almacena estado de sesión en memoria entre peticiones; cada llamada incluye sus cabeceras de autorización ('x-user-role', 'x-user-name').", "3. Comunicación Sin Estado (Stateless):"),
        bullet("Tanto payloads de entrada como respuestas se serializan en JSON UTF-8 universal.", "4. Mensajería JSON:"),
        bullet("Persistencia relacional embebida mediante SQLite ('tienda.db') con transacciones ACID y consultas preparadas (Prepared Statements).", "5. Persistencia Relacional:"),

        // 5. DIAGRAMAS ARQUITECTÓNICOS
        heading1("5. DIAGRAMAS ARQUITECTÓNICOS"),
        heading2("5.1. Diagrama de Capas de la Solución"),
        callout(
          "Estructura Lógica en Capas de la API REST",
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "| 1. CAPA DE PRESENTACIÓN WEB (SPA - HTML5 / CSS3 / ES6):                                                 |\n" +
          "|    - Pasajero: Registro público, Catálogo, Compra, Historial propio, Radicación PQRS de Cancelación.     |\n" +
          "|    - Superadministrador: Gestión de Rutas, Despacho, Gestión PQRS y Creación de Administradores.       |\n" +
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "                                                    |\n" +
          "                                                    v (HTTP/1.1 REST JSON - x-user-role / x-user-name)\n" +
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "| 2. CAPA PERIMETRAL Y GOBIERNO REST (Express Gateway & RBAC Middleware):                                  |\n" +
          "|    - Regla de Bloqueo: Si usuario tiene rol 'admin', rechaza login directo con HTTP 403 Forbidden.     |\n" +
          "|    - Middleware verificarRol(['superadmin']): Protege endpoints exclusivos de creación de admins.        |\n" +
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "                                                    |\n" +
          "                                                    v\n" +
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "| 3. CAPA DE SERVICIOS DE NEGOCIO (Controladores REST):                                                    |\n" +
          "|    [AuthService]       -> Login, registro público de pasajeros y validación de tokens.                   |\n" +
          "|    [SuperadminService] -> Aprovisionamiento y auditoría de administradores.                             |\n" +
          "|    [CatalogService]    -> Inventario de rutas, servicios y tarifas.                                     |\n" +
          "|    [BookingService]    -> Compra de pasajes y tiquetes con historial filtrado por rol.                   |\n" +
          "|    [DispatchService]   -> Planilla oficial de despacho para terminales y conductores.                    |\n" +
          "|    [SupportService]    -> Radicación de PQRS y aprobación con eliminación física de viajes.             |\n" +
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "                                                    |\n" +
          "                                                    v (Sentencias preparadas SQL / Prepared Statements)\n" +
          "+---------------------------------------------------------------------------------------------------------+\n" +
          "| 4. PERSISTENCIA RELACIONAL (SQLite / tienda.db):                                                        |\n" +
          "|    Tablas: `usuarios` (superadmin, admin, pasajeros) | `productos` (rutas y tarifas)                    |\n" +
          "|    `compras` (viajes activos) | `solicitudes_cancelacion` (PQRS y notas de resolución)                  |\n" +
          "+---------------------------------------------------------------------------------------------------------+"
        ),

        heading2("5.2. Flujo de Interacción: Creación de Administrador e Intento de Login Directo"),
        para("El siguiente flujo ilustra el control de seguridad implementado para la gestión de administradores:"),
        bullet("1. Superadministrador -> API REST: POST /api/v1/auth/login. Credenciales validadas, rol='superadmin' retornado.", "Paso 1:"),
        bullet("2. Superadministrador -> API REST: POST /api/v1/superadmin/crear-administrador con payload { usuario: 'admin_regional', clave: 'pass123' }.", "Paso 2:"),
        bullet("3. Backend inserta en la tabla `usuarios` con rol='admin' y confirma la creación.", "Paso 3:"),
        bullet("4. Usuario 'admin_regional' intenta loguearse en POST /api/v1/auth/login.", "Paso 4:"),
        bullet("5. Backend detecta rol='admin' y rechaza inmediatamente la petición con HTTP 403 Forbidden y mensaje: 'Los usuarios con rol Administrador no pueden iniciar sesión directamente en la aplicación'.", "Paso 5:"),

        // 6. PATRÓN THOMAS ERL
        heading1("6. PATRÓN ARQUITECTÓNICO Y FICHA TÉCNICA THOMAS ERL (2009)"),
        para("Ficha técnica formal del patrón Service Façade con RBAC aplicada a Expreso Bolivariano:"),
        new Table({
          width: { size: 100, type: WidthType.PERCENTAGE },
          borders: tableBorder,
          rows: [
            new TableRow({
              children: [
                headerCell("Característica del Patrón SOA", 30),
                headerCell("Especificación Aplicada en Expreso Bolivariano", 70)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("Nombre del Patrón", 30, true),
                bodyCell("Service Façade con RBAC y Aprovisionamiento Centralizado.", 70)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("Requerimiento / Problema Central", 30, true),
                bodyCell("¿Cómo permitir el registro público de pasajeros en la aplicación y garantizar que la creación de administradores sea exclusiva de una autoridad central (Superadmin), bloqueando el acceso directo a cuentas administrativas no autorizadas?", 70)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("Resumen: Solución", 30, true),
                bodyCell("Exponer una interfaz de servicios REST protegida por un middleware perimetral: el registro público solo otorga rol 'pasajero'; el endpoint de aprovisionamiento de administradores exige rol 'superadmin'; y el login intercepta y bloquea cualquier intento de autenticación directa con rol 'admin'.", 70)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("Impacto y Beneficios", 30, true),
                bodyCell("• Erradicación de creación clandestina de cuentas privilegiadas.\n• Imposibilidad de acceso directo para administradores (cero intrusiones por fuerza bruta).\n• Trazabilidad total de quién crea cada cuenta administrativa.", 70)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("Principios SOA Relacionados", 30, true),
                bodyCell("Abstracción de Servicios, Autonomía de Servicios y Contrato Estandarizado.", 70)
              ]
            })
          ]
        }),

        // 7. ESPECIFICACIÓN DETALLADA DE ENDPOINTS
        heading1("7. ESPECIFICACIÓN DETALLADA DE ENDPOINTS REST"),
        new Table({
          width: { size: 100, type: WidthType.PERCENTAGE },
          borders: tableBorder,
          rows: [
            new TableRow({
              children: [
                headerCell("Método", 12),
                headerCell("Endpoint URI", 33),
                headerCell("Rol Autorizado", 15),
                headerCell("Payload Entrada", 20),
                headerCell("Estructura Respuesta", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/auth/login", 33),
                bodyCell("Pasajero / Superadmin", 15),
                bodyCell("{ usuario, clave }", 20),
                bodyCell("{ exito, usuario, rol, token }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/auth/register", 33),
                bodyCell("Público (Pasajero)", 15),
                bodyCell("{ usuario, clave }", 20),
                bodyCell("{ exito, mensaje, rol: 'pasajero' }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/superadmin/crear-administrador", 33),
                bodyCell("Superadmin (403)", 15, true),
                bodyCell("{ usuario, clave }", 20),
                bodyCell("{ exito, mensaje, rol: 'admin' }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET", 12, true),
                bodyCell("/api/v1/superadmin/listar-administradores", 33),
                bodyCell("Superadmin (403)", 15, true),
                bodyCell("Ninguno", 20),
                bodyCell("[ { id, usuario, rol } ]", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET", 12, true),
                bodyCell("/api/v1/catalogo/rutas", 33),
                bodyCell("Pasajero / Superadmin", 15),
                bodyCell("Ninguno", 20),
                bodyCell("[ { id, nombre, precio } ]", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/catalogo/rutas", 33),
                bodyCell("Superadmin (403)", 15, true),
                bodyCell("{ nombre, precio }", 20),
                bodyCell("{ exito, mensaje }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("DELETE", 12, true),
                bodyCell("/api/v1/catalogo/rutas/:id", 33),
                bodyCell("Superadmin (403)", 15, true),
                bodyCell("URL param :id", 20),
                bodyCell("{ exito, mensaje }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/ventas/crear-orden", 33),
                bodyCell("Pasajero / Superadmin", 15),
                bodyCell("{ usuario, producto, cantidad }", 20),
                bodyCell("{ exito, compraId, tiqueteId }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET", 12, true),
                bodyCell("/api/v1/ventas/historial", 33),
                bodyCell("Filtrado RBAC", 15),
                bodyCell("Query ?usuario=...", 20),
                bodyCell("[ { id, usuario, producto, cantidad } ]", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/servicio-cliente/solicitar-cancelacion", 33),
                bodyCell("Pasajero", 15),
                bodyCell("{ usuario, compraId, motivo, contacto }", 20),
                bodyCell("{ exito, radicado, estado }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("POST", 12, true),
                bodyCell("/api/v1/servicio-cliente/gestionar-solicitud", 33),
                bodyCell("Superadmin (403)", 15, true),
                bodyCell("{ solicitudId, accion, respuesta }", 20),
                bodyCell("{ exito, mensaje, compraEliminada }", 20)
              ]
            }),
            new TableRow({
              children: [
                bodyCell("GET", 12, true),
                bodyCell("/api/v1/despachos/planilla", 33),
                bodyCell("Superadmin (403)", 15, true),
                bodyCell("Ninguno", 20),
                bodyCell("{ planillaId, bus, conductor, pasajeros }", 20)
              ]
            })
          ]
        }),

        // 8. REFERENCIAS BIBLIOGRÁFICAS
        heading1("8. REFERENCIAS BIBLIOGRÁFICAS"),
        bullet("Erl, T. (2009). SOA Design Patterns. Prentice Hall / Pearson Education.", "• "),
        bullet("Fielding, R. T. (2000). Architectural Styles and the Design of Network-based Software Architectures (Doctoral dissertation). University of California, Irvine.", "• "),
        bullet("Newman, S. (2021). Building Microservices: Designing Fine-Grained Systems (2nd ed.). O'Reilly Media.", "• "),
        bullet("Oliveros, D. (s.f.). Arquitecturas de Software orientada a servicios. Facultad de Ingeniería, diseño e innovación. Politécnico Grancolombiano. Material de estudio institucional.", "• "),
        bullet("Richards, M., & Ford, N. (2020). Fundamentals of Software Architecture: An Engineering Approach. O'Reilly Media.", "• "),
        bullet("Richardson, L., & Ruby, S. (2007). RESTful Web Services. O'Reilly Media.", "• ")
      ]
    }]
  });

  return doc;
}

async function main() {
  const doc = createDoc();
  const buffer = await Packer.toBuffer(doc);

  const pathSemana5 = 'ARQUITECTURA DE SOFTWARE ENTREGA 2 SEMANA 5.docx';
  fs.writeFileSync(pathSemana5, buffer);
  console.log(`Documento actualizado con éxito: ${pathSemana5} (${buffer.length} bytes)`);

  const pathSemana6 = 'ARQUITECTURA DE SOFTWARE ENTREGA 2 SEMANA 6.docx';
  try {
    fs.writeFileSync(pathSemana6, buffer);
    console.log(`Documento generado con éxito: ${pathSemana6} (${buffer.length} bytes)`);
  } catch (e) {
    if (e.code === 'EBUSY') {
      const altPath = 'ARQUITECTURA DE SOFTWARE ENTREGA 2 SEMANA 6_ACTUALIZADO.docx';
      fs.writeFileSync(altPath, buffer);
      console.log(`Nota: ${pathSemana6} está abierto en Microsoft Word por el usuario. Se guardó la versión actualizada en ${altPath}`);
    } else {
      throw e;
    }
  }
}

main().catch(console.error);
