curl -X POST "http://localhost:8080/api/token"
curl -X POST "http://localhost:8080/api/google" -d "input= ai researchs"
curl -X POST "http://localhost:8080/api/analyze" -d "input= ai researchs"
curl -X GET "http://localhost:8080/api/download" -o analysis.pdf   