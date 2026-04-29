import { Routes } from '@angular/router';

export const routes: Routes = [

    {
        path: 'auth',
        children: [
            {
                path: 'login',
                loadComponent: () => import('./pages/auth/login/login.component').then(m => m.LoginComponent),
                title: 'Entrar | Servio'
            }, {
                path: 'signup',
                loadComponent: () => import('./pages/auth/signup/signup.component').then(m => m.SignupComponent),
                title: 'Registrar | Servio'
            }

        ]
    },
];
