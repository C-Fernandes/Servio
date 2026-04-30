import { Routes } from '@angular/router';
import { LayoutComponent } from './layouts/layout/layout.component';

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
    }, {
        path: '',
        component: LayoutComponent,
        children: [
            {
                path: 'provider',
                loadComponent: () => import('./pages/dashboards/dashboard-provider/dashboard-provider.component').then(m => m.DashboardProviderComponent),
                title: 'Painel do Prestador | Servio'
            },
            {
                path: 'client',
                loadComponent: () => import('./pages/dashboards/dashboard-client/dashboard-client.component').then(m => m.DashboardClientComponent),
                title: 'Painel do Cliente | Servio'
            },

            {
                path: 'explore',
                loadComponent: () => import('./pages/marketplace/marketplace.component').then(m => m.MarketplaceComponent),
                title: 'Explorar Serviços | Servio'
            },
            {
                path: '',
                redirectTo: 'explore',
                pathMatch: 'full'
            }
        ]
    },
];
