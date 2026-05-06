import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth/auth.service';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { ToastService } from '../services/toast/toast.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const toast = inject(ToastService);

    const token = authService.getToken();

    let authReq = req;
    if (token) {
        authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 || error.status === 403) {


                toast.showToast('Sessão expirada ou sem permissão. Faça login novamente.', 'error');
                router.navigate(['auth/login']);
            }

            return throwError(() => error);
        })
    );
};