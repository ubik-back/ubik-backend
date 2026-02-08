import { Routes } from '@angular/router';
import { Home } from './views/home/home';
import { Explore } from './views/explore/explore';

import { MotelProfile } from './views/motel-profile/motel-profile';
import { ThreeButtom } from './views/three-buttom/three-buttom';
import { RoomsMotel } from './views/rooms-motel/rooms-motel';
import { RoomsOfferts } from './views/rooms-offerts/rooms-offerts';
import { TodosLosComponentes } from './todos-los-componentes/todos-los-componentes';
import { UserProfilePage } from './views/user-profile/user-profile';

/* login */
import { LoginComponent } from './views/Forms/login/login.component';


/** Register  */
import { RegisterUser } from './views/Forms/register/register-user/components/register-user';
import { RegisterSelect } from './views/Forms/register/register-select/register-select';
import { RegisterEstablishmentComponent } from './views/Forms/register/establecimiento/components/register-establishment';
import { RegisterPropertyEst } from './views/Forms/register/register-propertyEst/components/register-propertyEst';





export const routes: Routes = [

    {path: "", component: Home},
    {path: "explore", component: Explore},
    {path: "profile-motel", component: MotelProfile},
    {path: "three-buttons", component: ThreeButtom},// 🔴 ESTO DEBERIA DE LLAMARSE DASHBORAD-MOTEL - Y DEBERIA DE FUNCIONAR COMO UN FILTRP AL DARLE EN UN BOTON DEBERIA DE APARECER ABAJO LO QUE SE SELECCIONONO - POR EJEMPLO LA LISTA DE LOS MOTELES.
    
    {path: "rooms-motel", component: RoomsMotel},
    {path: "rooms-offerts", component: RoomsOfferts},
    
    /*========== REGISTER / LOGUIN ==========*/

    {path: "select-register", component: RegisterSelect},
    
    {path: "register-user", component: RegisterUser},
    {path: "register-propertyEst", component: RegisterPropertyEst},


    {path: 'login', component: LoginComponent},
    {path: "register-establishment", component: RegisterEstablishmentComponent},


    /**==== PERFILES DE USURIOS ====== */

    {path: "userProfile", component: UserProfilePage},


    /**⚠️ ESTA RUTA ES APARTE SOLO PARA VER TODOS LOS COMPONENTES DE LA PAGINA JUNTOS SOLO TENDREMOS CONOCIMIENTO DE ELLA NOSOTROS :v */
    {path: "allc", component: TodosLosComponentes},

]