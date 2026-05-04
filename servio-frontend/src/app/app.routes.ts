import { Routes } from '@angular/router';
import { LayoutComponent } from './layouts/layout/layout.component';
import { authGuard } from './guards/auth/auth-guard';

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
                path: 'service/details/:id',
                loadComponent: () => import('./pages/service-details/service-details.component').then(m => m.ServiceDetailsComponent),
                canActivate: [authGuard],
                title: 'Detalhes do Serviço | Servio'
            },
            {
                path: 'my-services',
                loadComponent: () => import('./pages/provider-services/provider-services.component').then(m => m.ProviderServicesComponent),
                canActivate: [authGuard],
                data: { roles: ['PROVIDER', 'ADMIN'] },
                title: 'Meus Serviços | Servio'
            },
            {
                path: 'provider',
                loadComponent: () => import('./pages/dashboards/dashboard-provider/dashboard-provider.component').then(m => m.DashboardProviderComponent),
                canActivate: [authGuard],
                data: { roles: ['PROVIDER', 'ADMIN'] },
                title: 'Painel do Prestador | Servio'
            },
            {
                path: 'client',
                loadComponent: () => import('./pages/dashboards/dashboard-client/dashboard-client.component').then(m => m.DashboardClientComponent),
                canActivate: [authGuard],
                title: 'Painel do Cliente | Servio'
            },
            { path: 'calendar', loadComponent: () => import('./pages/calendar/calendar.component').then(m => m.CalendarComponent), canActivate: [authGuard], data: { roles: ['PROVIDER', 'ADMIN'] }, title: 'Calendário | Servio' },
            {
                path: 'explore',
                loadComponent: () => import('./pages/marketplace/marketplace.component').then(m => m.MarketplaceComponent), canActivate: [authGuard],
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
