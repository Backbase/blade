import {Injectable} from '@angular/core';
import {Observable, timer, pipe} from 'rxjs';
import {Blade} from "../models/blade.model";
import {Graph} from "../models/graph.model";
import {HttpClient} from '@angular/common/http';
import {switchMap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class JolokiaService {

  constructor(protected http: HttpClient) {
  }

  getJvmStats(blade: Blade, graphs: Graph[]): Observable<any> {
    console.log("Start polling jvm stats for blade: " + blade.port);
    return timer(0, 1000)
      .pipe(
        switchMap(_ => this.executeJolokiaRequest(blade, graphs))
      );
  }

  executeJolokiaRequest(blade: Blade, graphs: Graph[]): Observable<any> {
    let jolokia = "http://" + window.location.hostname + ":" + blade.port + "/jolokia";
    let request = [];
    graphs.forEach(graph => {
      graph.dataset.forEach(dataset => {
        request.push({
          type: "read",
          mbean: dataset.mbean,
          attribute: dataset.attribute,
          path: dataset.path
        })
      })
    });
    return this.http.post(jolokia, request);
  }

}
