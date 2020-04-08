import {Observable, throwError} from 'rxjs';
import {catchError, publishReplay, refCount} from 'rxjs/operators';

import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Injectable} from "@angular/core";
import {Blade} from "../models/blade.model";
import {WebApp} from "../models/webapp.model";
import {WebSocketSubject} from 'rxjs/webSocket';
import {mergeWith} from 'lodash/mergeWith';

const API_URL = environment.apiUrl;

@Injectable()
export class BladeService {

  // subject: Subject<Blade[]> = new Subject();

  blades: Blade[];
  blades$: Observable<Blade[]>;


  constructor(protected http: HttpClient) {
    this.blades$ = this.http.get<Blade[]>(`${API_URL}/status`).pipe(
          publishReplay(1), refCount(),
          catchError((error: any) => throwError(error.message || 'Server error')),);
    this.blades$.subscribe(
      (blades) => {
        this.blades = blades
      });



  }

  // getBlades(): Observable<Blade[]> {
  //   return this.http.get<Blade[]>(`${API_URL}/status`, {
  //     headers: {'Content-Type': 'application/json'}
  //   }).;
  // }

  stop(blade: Blade, webApp: WebApp) {
    let bladeUrl = "http://" + window.location.hostname + ":" + blade.port + "/blade/api/stop" + webApp.name;
    return this.http.post(bladeUrl, "stop")
      .subscribe(next => {
          console.info(next);
        },
        error => {
          console.error(error)
        });
  }

  start(blade: Blade, webApp: WebApp) {
    let bladeUrl = "http://" + window.location.hostname + ":" + blade.port + "/blade/api/start" + webApp.name;
    return this.http.post(bladeUrl, "stop").subscribe(next => {
        console.info(next);
      },
      error => {
        console.error(error)
      });
  }

}

