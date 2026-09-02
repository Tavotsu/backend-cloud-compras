# Changelog

## [v0.1.0]
### Añadido
- Inicialización del microservicio `backend-cloud-compras` con Spring Boot (Web, Security, OAuth2 Resource Server).
- Configuración de `SecurityConfig` y `application.yml` para validación estricta de JWT de Azure AD (Issuer, Audience, y validación estricta del Rol "User").
- Integración con base de datos PostgreSQL usando Spring Data JPA (Entidad `Pedido` y `PedidoRepository`).
- `ComprasController` implementado extrayendo de forma segura el email del usuario autenticado directo del token JWT.
- `Dockerfile` multi-stage para compilar y ejecutar el proyecto optimizado en EC2.
- `README.md` con instrucciones precisas para levantar la BD y la app usando `docker run` en EC2 independiente.
