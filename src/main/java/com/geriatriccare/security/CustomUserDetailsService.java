package com.geriatriccare.security;

import com.geriatriccare.entity.User;
import com.geriatriccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find by username first, then by email (only active and non-deleted users)
        User user = userRepository.findByUsernameAndIsActiveTrue(usernameOrEmail)
                .or(() -> userRepository.findByEmailAndIsActiveTrue(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username or email: " + usernameOrEmail));

        return new UserPrincipal(user);
    }
    
    @Transactional
    public UserDetails loadUserById(String userId) {
        User user = userRepository.findByIdAndIsActiveTrue(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        return new UserPrincipal(user);
    }
}