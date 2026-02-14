#!/bin/bash

# Fix PasswordPolicyService - use setIsValid instead of setValid
sed -i 's/response\.setValid(/response.setIsValid(/g' src/main/java/com/geriatriccare/service/security/PasswordPolicyService.java

# Fix UserService - wrap Page returns with UserListResponse.from()
sed -i 's/return userRepository\.findAll(pageable)\.map(this::convertToResponse);/return UserListResponse.from(userRepository.findAll(pageable).map(this::convertToResponse));/g' src/main/java/com/geriatriccare/service/UserService.java

sed -i 's/return userRepository\.findByRole(role, pageable)\.map(this::convertToResponse);/return UserListResponse.from(userRepository.findByRole(role, pageable).map(this::convertToResponse));/g' src/main/java/com/geriatriccare/service/UserService.java

sed -i 's/return userRepository\.findByStatus(status, pageable)\.map(this::convertToResponse);/return UserListResponse.from(userRepository.findByStatus(status, pageable).map(this::convertToResponse));/g' src/main/java/com/geriatriccare/service/UserService.java

sed -i 's/return userRepository\.searchUsers(query, pageable)\.map(this::convertToResponse);/return UserListResponse.from(userRepository.searchUsers(query, pageable).map(this::convertToResponse));/g' src/main/java/com/geriatriccare/service/UserService.java

# Update return types in UserService
sed -i 's/public Page<UserResponse> getAllUsers/public UserListResponse getAllUsers/g' src/main/java/com/geriatriccare/service/UserService.java
sed -i 's/public Page<UserResponse> getUsersByRole/public UserListResponse getUsersByRole/g' src/main/java/com/geriatriccare/service/UserService.java
sed -i 's/public Page<UserResponse> getUsersByStatus/public UserListResponse getUsersByStatus/g' src/main/java/com/geriatriccare/service/UserService.java
sed -i 's/public Page<UserResponse> searchUsers/public UserListResponse searchUsers/g' src/main/java/com/geriatriccare/service/UserService.java

echo "✅ Fixed!"
mvn clean compile -DskipTests
