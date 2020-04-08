import {Component, Input, OnInit} from '@angular/core';
import {Graph} from "../../models/graph.model";
import {Observable} from 'rxjs';

@Component({
  selector: 'app-jvm-graph',
  template: `
    <div style="display: block;">
     <canvas baseChart width="400" height="300"
              [datasets]="graph.dataset"
              [options]="graph.options"
              [legend]="true"
              [chartType]="lineChartType"
             ></canvas></div>
  `,
  styleUrls: ['./jvm-graph.component.scss']
})
export class JvmGraphComponent implements OnInit {

  @Input() graph: Graph;
  @Input() data: Observable<any>;

  constructor() {
  }

  ngOnInit() {
    this.data.subscribe((data) => {
      data.forEach((response) => {
        this.graph.dataset = this.graph.dataset.map((dataset) => {
          if (response.request.mbean === dataset.mbean
            && response.request.path === dataset.path
            && response.request.attribute === dataset.attribute) {
            dataset.data.push({y: parseFloat(response.value), x: response.timestamp * 1000});
          }
          return dataset;
        });
      });
    })
  }
  public lineChartType: string = 'line';
}
