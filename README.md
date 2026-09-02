# Microservicio Compras

Este microservicio gestiona el registro de pedidos de forma privada, validando roles con Azure AD.

## Despliegue en EC2 con Docker Run

Para desplegar este microservicio junto con su base de datos usando `docker run`, sigue estos pasos en tu instancia EC2:

1. **Crear una red de Docker** para que los contenedores se comuniquen:
   ```bash
   docker network create compras-net
   ```

2. **Levantar la base de datos PostgreSQL**:
   ```bash
   docker run -d \
     --name compras_db \
     --network compras-net \
     -e POSTGRES_USER=admin \
     -e POSTGRES_PASSWORD=password123 \
     -e POSTGRES_DB=compras_db \
     -v compras_db_data:/var/lib/postgresql/data \
     postgres:15-alpine
   ```

3. **Construir la imagen del microservicio**:
   ```bash
   docker build -t compras_app .
   ```

4. **Levantar el microservicio**:
   ```bash
   docker run -d \
     --name compras_app \
     --network compras-net \
     -p 8082:8082 \
     -e DB_HOST=compras_db \
     -e DB_PORT=5432 \
     -e DB_NAME=compras_db \
     -e DB_USER=admin \
     -e DB_PASS=password123 \
     compras_app
   ```
