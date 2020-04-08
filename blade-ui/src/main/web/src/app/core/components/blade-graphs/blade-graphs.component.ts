import {Component, OnInit, Input} from '@angular/core';
import {Blade} from "../../models/blade.model";
import {JolokiaService} from "../../services/jolokia.service";
import {Graph} from "../../models/graph.model";
import {Observable} from 'rxjs';

@Component({
  selector: 'app-blade-graphs',
  template: `
    <div class="row">
        <app-jvm-graph *ngFor="let graph of graphs"  [graph]="graph" [data]="data" class="col-3"></app-jvm-graph>
    </div>
  `,
  styleUrls: ['./blade-graphs.component.scss'],
  providers: [JolokiaService]
})
export class BladeGraphsComponent implements OnInit {

  @Input() blade: Blade;

  data: Observable<any>;


  graphs: Graph[];




  constructor(protected jolokiaService: JolokiaService) {
    let backgroundColor = [
      'rgba(255, 99, 132, 0.2)',
      'rgba(54, 162, 235, 0.2)',
      'rgba(255, 206, 86, 0.2)',
      'rgba(75, 192, 192, 0.2)',
      'rgba(153, 102, 255, 0.2)',
      'rgba(255, 159, 64, 0.2)'
    ];
    let borderColor =  [
      'rgba(255,99,132,1)',
      'rgba(54, 162, 235, 1)',
      'rgba(255, 206, 86, 1)',
      'rgba(75, 192, 192, 1)',
      'rgba(153, 102, 255, 1)',
      'rgba(255, 159, 64, 1)'
    ];


    this.graphs = [
      {
        title: 'Heap Memory',
        options: this.memoryOptions,
        dataset: [{
          label: "committed",
          type: "line",
          mbean: 'java.lang:type=Memory',
          attribute: 'HeapMemoryUsage',
          path: 'committed',
          data: [],
          pointRadius: 0,
          backgroundColor: backgroundColor[0],
          borderColor: borderColor[0]
        }, {
          label: "used",
          type: "line",
          mbean: 'java.lang:type=Memory',
          attribute: 'HeapMemoryUsage',
          path: 'used',
          data: [],
          pointRadius: 0,
          backgroundColor: backgroundColor[1],
          borderColor: borderColor[1]
        }],
      }, {
        title: 'Non Heap Memory',
        options: this.memoryOptions,
        dataset: [{
          label: 'committed',
          type: "line",
          mbean: 'java.lang:type=Memory',
          attribute: 'NonHeapMemoryUsage',
          path: 'committed',
          data: [],
          pointRadius: 0,
          backgroundColor: backgroundColor[2],
          borderColor: borderColor[2]
        }, {
          label: "used",
          type: "line",
          mbean: 'java.lang:type=Memory',
          attribute: 'NonHeapMemoryUsage',
          path: 'used',
          data: [],
          pointRadius: 0,
          backgroundColor: backgroundColor[3],
          borderColor: borderColor[3]
        },
        ]
      }, {
        title: 'System Load Average',
        options: this.defaultOptions,
        dataset: [
          {
            label: "System Load",
            type: "line",
            mbean: 'java.lang:type=OperatingSystem',
            attribute: "SystemLoadAverage",
            pointRadius: 0,
            data: [],
            backgroundColor: backgroundColor[4],
            borderColor: borderColor[4]
          }
        ]
      }, {
        title: 'Threads',
        options: this.defaultOptions,
        dataset: [
          {
            label: "Thread Count",
            type: "line",
            mbean: 'java.lang:type=Threading',
            attribute: "ThreadCount",
            pointRadius: 0,
            data: [],
            backgroundColor: backgroundColor[5],
            borderColor: borderColor[5]
          }
        ]
      }
    ];
  }

  ngOnInit() {
    this.data = this.jolokiaService.getJvmStats(this.blade, this.graphs);
  }

  private memoryOptions = {
    responsive: true,
    legend: {
      display: false
    },
    scales: {
      xAxes: [{
        gridLines: {
          display: false
        },
        type: 'time',
        time: {
          parser: 'HH:mm',
        },
        scaleLabel: {
          display: true,
          label: 'bla'
        }
      }],
      yAxes: [{
        gridLines: {
          display: false
        },
        ticks: {
          // Include a dollar sign in the ticks
          callback: function (value, index, values) {
            return (value / 1000000) + " MB";
          }
        },
        scaleLabel: {
          display: true,
          labelString: 'MB'
        }
      }]
    },
  };

  private defaultOptions = {
    responsive: true,
    legend: {
      display: false
    },
    scales: {
      xAxes: [{
        gridLines: {
          display: false
        },
        type: 'time',
        time: {
          parser: 'HH:mm',
        },
        scaleLabel: {
          display: false
        }
      }],
      yAxes: [{
        gridLines: {
          display: false
        },
        scaleLabel: {
          display: true
        }
        ,
        ticks: {
          beginAtZero: true
        }
      }]
    },
  };




}
