#!/bin/bash
# Stop Docker containers

echo "🛑 Stopping Geriatric Care System..."

# Check which compose file is running
if docker-compose ps | grep -q "geriatric-care"; then
    docker-compose down
    echo "✅ Development environment stopped"
elif docker-compose -f docker-compose.prod.yml ps | grep -q "geriatric-care"; then
    docker-compose -f docker-compose.prod.yml down
    echo "✅ Production environment stopped"
else
    echo "ℹ️  No containers running"
fi
