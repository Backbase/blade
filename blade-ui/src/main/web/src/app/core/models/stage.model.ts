import {WebApp} from "./webapp.model";

export interface Stage {
  id: string;
  name: string;
  autoStart: boolean;
  started: boolean;
  multiThreaded: boolean;
  webApps: WebApp[];


}
