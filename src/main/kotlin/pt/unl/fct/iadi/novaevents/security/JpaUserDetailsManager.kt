package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.model.AppRole
import pt.unl.fct.iadi.novaevents.model.AppRoleName
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.repository.AppRoleRepository
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository

@Service
class JpaUserDetailsManager(
    private val appUserRepository: AppUserRepository,
    private val appRoleRepository: AppRoleRepository
) : UserDetailsManager {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val appUser = appUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User '$username' not found")

        val authorities = appUser.roles
            .map { SimpleGrantedAuthority(it.role.name) }

        return User.withUsername(appUser.username)
            .password(appUser.password)
            .authorities(authorities)
            .build()
    }

    @Transactional
    override fun createUser(user: UserDetails) {
        if (userExists(user.username)) {
            throw IllegalArgumentException("User '${user.username}' already exists")
        }

        val appUser = appUserRepository.save(
            AppUser(
                username = user.username,
                password = user.password
            )
        )

        val roles = user.authorities
            .mapNotNull { authorityToRole(it) }
            .ifEmpty { listOf(AppRoleName.ROLE_EDITOR) }
            .map { role -> AppRole(role = role, user = appUser) }

        appRoleRepository.saveAll(roles)
    }

    @Transactional
    override fun updateUser(user: UserDetails) {
        val existing = appUserRepository.findByUsername(user.username)
            ?: throw UsernameNotFoundException("User '${user.username}' not found")

        existing.password = user.password
        appUserRepository.save(existing)

        val newRoles = user.authorities
            .mapNotNull { authorityToRole(it) }
            .ifEmpty { listOf(AppRoleName.ROLE_EDITOR) }

        appRoleRepository.deleteAll(existing.roles)
        existing.roles.clear()

        val roleEntities = newRoles
            .map { role -> AppRole(role = role, user = existing) }

        appRoleRepository.saveAll(roleEntities)
    }

    @Transactional
    override fun deleteUser(username: String) {
        val user = appUserRepository.findByUsername(username) ?: return
        appUserRepository.delete(user)
    }

    @Transactional(readOnly = true)
    override fun changePassword(oldPassword: String?, newPassword: String?) {
        throw UnsupportedOperationException("Password changes are not implemented in this assignment")
    }

    @Transactional(readOnly = true)
    override fun userExists(username: String): Boolean =
        appUserRepository.existsByUsername(username)

    private fun authorityToRole(authority: GrantedAuthority): AppRoleName? =
        runCatching { AppRoleName.valueOf(authority.authority) }.getOrNull()
}

