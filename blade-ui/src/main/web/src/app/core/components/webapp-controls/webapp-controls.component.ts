import {Component, Input, OnInit} from '@angular/core';
import {WebApp} from "../../models/webapp.model";
import {Blade} from "../../models/blade.model";
import {BladeService} from "../../services/blade.service";

@Component({
  selector: 'app-webapp-controls',
  template: `
    <button class="btn btn-sm btn-success start" *ngIf="isStopped()"
                      (click)="start()">Start
              </button>
              <button class="btn btn-sm btn-danger stop" *ngIf="!isStopped()"
                      (click)="stop()">Stop
              </button>
  `,
  styleUrls: ['./webapp-controls.component.scss']
})
export class WebappControlsComponent implements OnInit {

  constructor(public bladeService: BladeService) {
  }

  ngOnInit() {
  }

  @Input() webApp: WebApp;
  @Input() blade: Blade;

  isStopped() {
    return !this.isLoading() && this.webApp.state === 'STOPPED';
  }

  isStarted() {
    return !this.isLoading() && this.webApp.state === 'STARTED';
  }

  stop() {
    this.bladeService.stop(this.blade, this.webApp);
  }

  start() {
    this.bladeService.start(this.blade, this.webApp);
  }

  isLoading() {
    switch (this.webApp.state) {
      case "STARTING_PREP":
      case "STARTING":
        return true;
      default:
        return false;
    }
  }

}
