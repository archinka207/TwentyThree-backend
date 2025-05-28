package com.twentythree.messenger.config;

// import com.twentythree.messenger.security.UserPrincipal; // If using UserPrincipal
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing // Can be here or on main app class
public class JpaAuditingConfig {

    // Optional: If you want to set createdBy/updatedBy fields automatically
    // @Bean
    // public AuditorAware<Long> auditorProvider() {
    //     return new SpringSecurityAuditAwareImpl();
    // }
}

// class SpringSecurityAuditAwareImpl implements AuditorAware<Long> {
//     @Override
//     public Optional<Long> getCurrentAuditor() {
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         if (authentication == null ||
//             !authentication.isAuthenticated() ||
//             authentication instanceof AnonymousAuthenticationToken) {
//             return Optional.empty();
//         }
//         // Assuming UserPrincipal implements org.springframework.security.core.userdetails.UserDetails
//         // And has a getId() method
//         // UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//         // return Optional.ofNullable(userPrincipal.getId());
//         // For now, if you don't have UserPrincipal with ID, you might return a default or skip this.
//         return Optional.empty(); // Placeholder
//     }
// }