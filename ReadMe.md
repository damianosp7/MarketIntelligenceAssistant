
# WatsonXAI Project

## Overview

This project is a Spring Boot application that integrates with IBM Watson services to fetch Google search results, generate analysis based on those results, and create PDFs from the generated analysis.

## Features

- Fetch Google search output using a provided input string.
- Generate analysis based on Google search results with the help of an AI Agent.
- Create PDFs from the generated analysis.
- Handle text formatting and token management.

## Technologies Used

- Java
- Spring Boot
- Maven
- IBM Watson
- PDFBox
- JSON

## Endpoints

### Fetch Access Token

- **URL**: `/api/token`
- **Method**: POST
- **Description**: Fetches a new access token from the IBM Cloud IAM service.
   ```bash
    curl -X POST "http://localhost:8080/api/token"
### Fetch Google Search Output

- **URL**: `/api/google`
- **Method**: POST
- **Parameters**: `input` (String) - The input string for the Google search.
- **Description**: Fetches the Google search output using the provided input string.
   ```bash
  curl -X POST "http://localhost:8080/api/google" -d "input= electric vehicles" 

### Generate Analysis Based on Search

- **URL**: `/api/analyze`
- **Method**: POST
- **Parameters**: `input` (String) - The input string for the Google search and analysis.
- **Description**: Generates an analysis based on the Google search output for the given input string with the help of the the AI agent
   ```bash
    curl -X POST "http://localhost:8080/api/analyze" -d "input= electric vehicles"

### Download PDF

- **URL**: `/api/download`
- **Method**: GET
- **Description**: Downloads the generated PDF from the analysis.
   ```bash
  curl -X GET "http://localhost:8080/api/download" -o analysis.pdf
  

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-directory>

2. Update the application.properties file with your IBM Watson credentials and URLs.
3. Build the project using Maven:  
   ```bash
    mvn clean install
    Run the application:  
    mvn spring-boot:run
### Usage
- ***Use the /api/token endpoint to fetch an access token.***
- ***Use the /api/google endpoint to fetch Google search output.***
- ***Use the /api/analyze endpoint to generate analysis based on the search output.***
- ***Use the /api/download endpoint to download the generated PDF.***

## AI Research Agent specializing in Market Intelligence

You are an AI Research Assistant specializing in Market Intelligence. Your role is to provide comprehensive, data-driven insights. You excel at gathering, analyzing, and synthesizing market data, competitive intelligence, and industry trends.

### Capabilities & Responsibilities

- **Market Analysis**: Identify emerging market trends, growth opportunities, and potential threats.
- **Competitive Intelligence**: Track competitor strategies, strengths, weaknesses, market positioning, and product offerings.
- **Consumer Insights**: Analyze consumer behavior, preferences, and purchasing trends.
- **Industry Reports**: Summarize and create detailed reports on market size, segmentation, and forecasts.
- **SWOT & PESTEL Analysis**: Conduct in-depth analyses to evaluate internal and external factors impacting business.
- **Strategic Recommendations**: Provide actionable insights and recommendations to drive business growth.
- **Data Interpretation**: Translate complex data into clear, concise, and visually appealing reports.

### Persona & Tone

- Analytical, Insightful, and Professional
- Communicates complex information clearly and precisely
- Adaptable to different industries and market contexts
- Objective and data-driven

### Format

Your responses should include:
- Executive Summary
- Detailed Analysis
- Strategic Recommendations
- SWOT and PESTEL Analysis
- Competitive Landscape
- Consumer Insights
- Market Trends & Forecasts
- 
## Tools Used from WatsonXAI

- **Model ID**: ibm/granite-3-8b-instruct
- **Agent Prompt**: AI Research Assistant specializing in Market Intelligence fine tuning
- **Decoding Method**: Greedy
- **Max New Tokens**: 2500

## Tools Used from Utility Agents

- **Google**: Used for fetching Google search results.
- **WebCrawler**: Used for accessing Google search urls.