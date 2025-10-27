# API Service

This is a Spring Boot application that provides the following endpoints:

## Endpoints

### 1. Screen Configuration
- **URL**: `POST /api/screen_configuration`
- **Headers**: 
  - `channel`: Channel identifier
  - `Accept-Language`: Language (EN/AR)
  - `serviceId`: Service identifier
  - `screenId`: Screen identifier
  - `moduleId`: Module identifier
  - `subModuleId`: Sub-module identifier
- **Body**: BaseServiceRequest with deviceInfo

### 2. Profit Rate
- **URL**: `POST /api/profitRate`
- **Headers**: Same as above
- **Body**: BaseServiceRequest with deviceInfo

### 3. Exchange Rate
- **URL**: `POST /api/exchangeRate` or `POST /api/exchangeRate/{currencyCode}`
- **Headers**: Same as above
- **Body**: BaseServiceRequest with deviceInfo

### 4. Place Request
- **URL**: `POST /api/place_request/{serviceName}`
- **Headers**: Same as above
- **Body**: BaseServiceRequest with deviceInfo

## Running the Application

1. **Build the project**:
   ```bash
   mvn clean compile
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **The application will start on port 9999**

## Testing the Endpoints

You can test the endpoints using curl or any HTTP client. Here's an example:

```bash
curl -X POST http://localhost:9999/api/screen_configuration \
  -H "Content-Type: application/json" \
  -H "channel: WEB" \
  -H "Accept-Language: EN" \
  -H "serviceId: TEST" \
  -H "screenId: TEST_SCREEN" \
  -H "moduleId: TEST_MODULE" \
  -H "subModuleId: TEST_SUB_MODULE" \
  -d '{
    "request": {
      "requestInfo": {},
      "deviceInfo": {
        "deviceId": "test-device"
      }
    }
  }'
```

## Features

- **Device Info Validation**: All endpoints require device information in the request
- **Mock Responses**: The application returns mock responses for demonstration purposes
- **Multi-language Support**: Supports English (EN) and Arabic (AR) languages
- **Field Validation**: Includes field validation for English language requests
- **Grouped Responses**: Profit rate endpoint returns grouped responses by product type

## Configuration

The application is configured to run on port 9999 as specified in `application.yml`.
