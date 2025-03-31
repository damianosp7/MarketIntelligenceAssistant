## Example 1: Ai stocks

### Generate Analysis
   ``
   curl -X POST "http://localhost:8080/api/analyze" -d "input= ai stocks"
   ``
### Download PDF
   ``curl -X GET "http://localhost:8080/api/download" -o analysisEV.pdf
    ``

## Example 2: dubai chocolate trend

### Generate Analysis
``
curl -X POST "http://localhost:8080/api/analyze" -d "input= dubai chocolate trend"
``
### Download PDF
``
curl -X GET "http://localhost:8080/api/download" -o analysisDubai.pdf
``



