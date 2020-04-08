import {Component, Input, OnInit} from '@angular/core';
import {WebApp} from "../../models/webapp.model";

@Component({
  selector: 'app-spinner',
  template: '<div *ngIf="isLoading()"><img src="assets/icons/sword.gif" width="20" /></div>',
  styleUrls: ['./spinner.component.scss']
})
export class SpinnerComponent implements OnInit {

  constructor() {
  }

  ngOnInit() {
  }

  @Input() webApp: WebApp;

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
