import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: 'auth',
        children: [
            {
                path: 'login',
                loadComponent: () => import('./pages/auth/login/login.component').then(m => m.LoginComponent),
                title: 'Entrar | Servio'
            },

        ]
    },
];
