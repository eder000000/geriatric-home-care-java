#!/bin/bash
# Start production environment with Docker

echo "🚀 Starting Geriatric Care System (Production)..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found. Please create it first."
    echo "   Copy .env.example to .env and update the values."
    exit 1
fi

# Load environment variables
source .env

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Build and start containers
docker-compose -f docker-compose.prod.yml up --build -d

# Wait for application to be healthy
echo "⏳ Waiting for application to start..."
sleep 20

# Check health
if docker-compose -f docker-compose.prod.yml ps | grep -q "Up (healthy)"; then
    echo "✅ Application is running in production mode!"
    echo ""
    echo "📚 Access points:"
    echo "   - Application: http://localhost:8080"
    echo ""
    echo "📋 View logs: docker-compose -f docker-compose.prod.yml logs -f app"
    echo "🛑 Stop: docker-compose -f docker-compose.prod.yml down"
else
    echo "⚠️  Application may still be starting. Check logs:"
    echo "   docker-compose -f docker-compose.prod.yml logs -f app"
fi
