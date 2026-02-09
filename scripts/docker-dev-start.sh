#!/bin/bash
# Start development environment with Docker

echo "🚀 Starting Geriatric Care System (Development)..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Build and start containers
docker-compose up --build -d

# Wait for application to be healthy
echo "⏳ Waiting for application to start..."
sleep 10

# Check health
if docker-compose ps | grep -q "Up (healthy)"; then
    echo "✅ Application is running!"
    echo ""
    echo "📚 Access points:"
    echo "   - Application: http://localhost:8080"
    echo "   - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   - Database: localhost:5432"
    echo ""
    echo "📋 View logs: docker-compose logs -f app"
    echo "🛑 Stop: docker-compose down"
else
    echo "⚠️  Application may still be starting. Check logs:"
    echo "   docker-compose logs -f app"
fi
