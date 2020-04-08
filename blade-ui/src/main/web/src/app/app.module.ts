import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import {AppComponent} from "./core/components/app/app.component";
import {HeaderComponent} from "./core/components/header/header.component";
import {FooterComponent} from "./core/components/footer/footer.component";
import {HttpClientModule} from "@angular/common/http";
import {SpinnerComponent} from './core/components/spinner/spinner.component';
import {WebappControlsComponent} from './core/components/webapp-controls/webapp-controls.component';
import { JvmGraphComponent } from './core/components/jvm-graph/jvm-graph.component';
import {ChartsModule} from 'ng2-charts/ng2-charts';
import { BladeGraphsComponent } from './core/components/blade-graphs/blade-graphs.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    SpinnerComponent,
    WebappControlsComponent,
    JvmGraphComponent,
    BladeGraphsComponent
  ],
  imports: [
    NgbModule,
    BrowserModule,
    HttpClientModule,
    ChartsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
