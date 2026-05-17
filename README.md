 ---
# ForPadelist — Aplicación de Reserva de Pistas de Pádel

Práctica Final de la asignatura **Programación de Aplicaciones Telemáticas**

  ---

## Integrantes del equipo

| Nombre | Apellidos |
  |--------|-----------|
| Ignacio | Yubero |
| Mateo | Mascaraque |
| Rodrigo | Orozco |
| Ignacio | Gutiérrez |
| Marco | Claudio |

  ---

## Descripción del proyecto

**ForPadelist** es una aplicación web full-stack para la gestión y reserva de pistas de pádel. Permite a los usuarios consultar la disponibilidad
de pistas, realizar reservas y gestionarlas, mientras que los administradores disponen de herramientas para gestionar el catálogo de pistas y
supervisar todas las reservas del sistema.

  ---

## Funcionalidades

### Usuario no autenticado
- Consultar el listado de pistas disponibles
- Ver la disponibilidad por fecha y pista
- Registrarse e iniciar sesión

### Usuario autenticado (rol USER)
- Realizar reservas seleccionando pista, fecha, hora y duración
- Consultar sus reservas activas e historial
- Cancelar reservas con al menos 24 horas de antelación
- Modificar reservas existentes
- Actualizar sus datos de perfil

### Administrador (rol ADMIN)
- Crear, modificar y eliminar pistas
- Consultar todas las reservas del sistema con filtros por fecha, pista o usuario
- Gestionar el estado de las pistas (activa/inactiva)
- Acceso completo a la gestión de usuarios

  ---



## API — Endpoints principales

| Método | Ruta | Descripción | Rol |
  |--------|------|-------------|-----|
| POST | `/pistaPadel/auth/register` | Registro de usuario | Público |
| POST | `/pistaPadel/auth/login` | Inicio de sesión | Público |
| POST | `/pistaPadel/auth/logout` | Cierre de sesión | Autenticado |
| GET | `/pistaPadel/auth/me` | Datos del usuario actual | Autenticado |
| GET | `/pistaPadel/courts` | Listado de pistas | Público |
| POST | `/pistaPadel/courts` | Crear pista | Admin |
| PATCH | `/pistaPadel/courts/{id}` | Modificar pista | Admin |
| DELETE | `/pistaPadel/courts/{id}` | Eliminar pista | Admin |
| GET | `/pistaPadel/availability` | Disponibilidad por fecha | Público |
| POST | `/pistaPadel/reservations` | Crear reserva | Autenticado |
| GET | `/pistaPadel/reservations` | Mis reservas | Autenticado |
| PATCH | `/pistaPadel/reservations/{id}` | Modificar reserva | Autenticado |
| DELETE | `/pistaPadel/reservations/{id}` | Cancelar reserva | Autenticado |
| GET | `/pistaPadel/admin/reservations` | Todas las reservas | Admin |
| GET | `/pistaPadel/users` | Listado de usuarios | Admin |
| GET | `/pistaPadel/health` | Estado de la API | Público |

  ---

## Cómo ejecutar el proyecto

### Requisitos previos
- Java 21 o superior
- Maven 3.8 o superior

Ejecutar los tests

./mvnw test

  ---
Usuarios de prueba

▎ Los usuarios se crean automáticamente al arrancar la aplicación.

┌───────────────┬──────────────────────────────────────────┬────────────┐
│      Rol      │                  Email                   │ Contraseña │
├───────────────┼──────────────────────────────────────────┼────────────┤
│ Administrador │ admin@padelapp.com                       │ admin123   │
├───────────────┼──────────────────────────────────────────┼────────────┤
│ Usuario       │ (registrar cuenta nueva desde /register) │ —          │
└───────────────┴──────────────────────────────────────────┴────────────┘

  ---
Reglas de negocio destacadas

- Las pistas están disponibles de 9:00 a 22:00 en slots de 30 minutos
- Las reservas solo se pueden cancelar con un mínimo de 24 horas de antelación
- No se puede eliminar una pista que tenga reservas activas
- El sistema detecta y rechaza automáticamente solapamientos de reservas