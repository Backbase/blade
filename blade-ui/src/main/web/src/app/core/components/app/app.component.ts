import {Component, OnInit} from '@angular/core';
import {Blade} from "../../models/blade.model";
import {BladeService} from "../../services/blade.service";
import {WebSocketSubject} from "rxjs/webSocket";
import {environment} from "../../../../environments/environment";
import {WebApp} from "../../models/webapp.model";
import {Graph} from "../../models/graph.model";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  providers: [BladeService]
})
export class AppComponent implements OnInit {

  blades: Blade[];
  domainPrefix=window.location.protocol+"//" + window.location.hostname+":";
  isLoading: boolean = true;

  constructor(public bladeService: BladeService) {
    this.isLoading = true;
  }

  ngOnInit() {
    let socket$;
    if (environment.wsUrl.startsWith("/")) {
      socket$ = new WebSocketSubject<Blade[]>((window.location.protocol === "https:" ? "wss://" : "ws://") + window.location.host + window.location.pathname + environment.wsUrl);
    } else {
      socket$ = new WebSocketSubject<Blade[]>(environment.wsUrl);
    }


    socket$.subscribe(
      (blades) => {
        this.isLoading = false;
        this.blades = blades;
      },
      (err) => console.log(err),
      () => console.log('complete'));
  }

  tableClass(webApp: WebApp) {
    return "table-" + this.stateClass(webApp);
  }

  badgePillClass(webApp: WebApp) {
    return "badge badge-pill badge-" + this.stateClass(webApp);
  }

  stateClass(webApp: WebApp) {
    switch (webApp.state) {
      case "INITIALIZING":
      case "STARTED":
        return "success";
      case "STARTING_PREP":
      case "STARTING":
        return "info";
      case "STOPPING_PREP":
      case "STOPPING" :
        return "warning";
      case "FAILED" :
        return "danger";
      default:
        return "secondary";
    }
  }

  trackByBlade = (blade) => blade.id;
  trackByStage = (stage) => stage.id;
  trackByWebApp = (webApp) => webApp.id;


}
