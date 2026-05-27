# =============================================================================
# DataPipeline - SeaTunnel Custom Image
# =============================================================================
# Extends the official SeaTunnel image with pre-installed JDBC connector plugin
# and database drivers required for data synchronization tasks.

FROM apache/seatunnel:2.3.8

# ---------------------------------------------------------------------------
# Step 1: Install the JDBC connector plugin via the official install script
# ---------------------------------------------------------------------------
# Override plugin_config to download only the JDBC connector (not all plugins),
# which keeps the image size minimal.
COPY plugin_config /opt/seatunnel/config/plugin_config

RUN bash /opt/seatunnel/bin/install-plugin.sh 2.3.8

# ---------------------------------------------------------------------------
# Step 2: Download database-specific JDBC drivers
# ---------------------------------------------------------------------------
# The connector-jdbc plugin does NOT bundle database drivers due to licensing.
# We must provide them manually for each supported database type.

# MySQL 8.x JDBC Driver
ADD https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar /opt/seatunnel/lib/mysql-connector-java-8.0.28.jar

# PostgreSQL JDBC Driver
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar /opt/seatunnel/lib/postgresql-42.7.3.jar
