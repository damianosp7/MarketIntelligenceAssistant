## AI-Powered Virtual Agents

### Model Integration
- **Model ID**: `ibm/granite-3-8b-instruct`
- The project integrates IBM Watsonx.ai models to process and analyze data, providing comprehensive insights.

### Agent Prompt
- The virtual agent is fine-tuned with a specific prompt: "AI Research Assistant specializing in Market Intelligence."
- This prompt ensures the agent is tailored to gather, analyze, and synthesize market data, competitive intelligence, and industry trends.

### Decoding Method
- **Decoding Method**: Greedy
- The agent uses a greedy decoding method to generate responses, ensuring the most relevant and coherent output.

### Token Management
- **Max New Tokens**: 2500
- The agent is configured to generate up to 2500 new tokens, allowing for detailed and comprehensive analysis.

## Capabilities & Responsibilities

- **Market Analysis**: The agent identifies emerging market trends, growth opportunities, and potential threats.
- **Competitive Intelligence**: It tracks competitor strategies, strengths, weaknesses, market positioning, and product offerings.
- **Consumer Insights**: The agent analyzes consumer behavior, preferences, and purchasing trends.
- **Industry Reports**: It summarizes and creates detailed reports on market size, segmentation, and forecasts.
- **SWOT & PESTEL Analysis**: The agent conducts in-depth analyses to evaluate internal and external factors impacting business.
- **Strategic Recommendations**: It provides actionable insights and recommendations to drive business growth.
- **Data Interpretation**: The agent translates complex data into clear, concise, and visually appealing reports.

## Utility Agents

- **Google**: Used for fetching Google search results.
- **WebCrawler**: Used for accessing Google search URLs.

## Endpoints

- **/api/google**: Fetches Google search output using the provided input string.
- **/api/analyze**: Generates analysis based on the Google search output.
- **/api/download**: Downloads the generated PDF from the analysis.

By integrating IBM Watsonx.ai and utility agents like Google and WebCrawler, our project ensures that the virtual agents provide accurate, data-driven insights, enhancing the overall market intelligence capabilities.