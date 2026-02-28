# Curl Commands for User Role Management
# Base URL: http://localhost:8080
# All endpoints require JWT Bearer token authentication

# Replace {userId} with the actual user ID
# Replace {jwt_token} with a valid JWT token from login

## Promote User to ADMIN
curl -X PUT "http://localhost:8080/api/admin/users/{userId}/role" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{"role": "ADMIN"}'

## Promote User to SUPER_ADMIN
curl -X PUT "http://localhost:8080/api/admin/users/{userId}/role" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{"role": "SUPER_ADMIN"}'

## Demote User to USER
curl -X PUT "http://localhost:8080/api/admin/users/{userId}/role" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{"role": "USER"}'

## Example with actual values
# Assuming user ID is "123e4567-e89b-12d3-a456-426614174000"
# And JWT token is obtained from login endpoint
curl -X PUT "http://localhost:8080/api/admin/users/123e4567-e89b-12d3-a456-426614174000/role" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{"role": "ADMIN"}'

## Error Responses
# 401 Unauthorized - Invalid or missing JWT token
# 403 Forbidden - Insufficient permissions (e.g., ADMIN trying to promote to SUPER_ADMIN)
# 404 Not Found - User not found
# 400 Bad Request - Invalid role or locked user
# 409 Conflict - Attempting to demote SUPER_ADMIN

## Authorization Rules
# - SUPER_ADMIN can promote/demote to any role (except demoting other SUPER_ADMINs)
# - ADMIN can only promote/demote to USER or ADMIN
# - USER cannot change roles
# - Cannot promote locked users
# - Cannot demote SUPER_ADMIN users</content>
<parameter name="filePath">/Users/yusuf.ibrahim/Projects/neurixa/CURL-ROLE-MANAGEMENT.md
