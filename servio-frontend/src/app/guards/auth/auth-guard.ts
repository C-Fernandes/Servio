

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { ToastService } from '../../services/toast/toast.service';
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const toast = inject(ToastService);
  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  const expectedRoles = route.data['roles'] as string[];

  if (!expectedRoles || expectedRoles.length === 0) {
    return true;
  }

  const userRole = authService.getUserRole();
  if (userRole && expectedRoles.includes(userRole)) {
    return true;
  }
  toast.showToast("Você não tem permissão para acessar essa rota", "error");
  console.warn(`Acesso negado: Usuário com role ${userRole} tentou acessar rota restrita.`);
  return router.createUrlTree(['/explore']);
};