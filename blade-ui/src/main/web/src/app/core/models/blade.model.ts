import {Stage} from "./stage.model";

export interface Blade {
  id: string;
  name: string;
  port: number;
  securePort: number;
  isStarting: boolean;
  isReady: boolean;
  isRunning: boolean;
  startedOn: Date;
  stages: Stage[];
  bladeMaster: string;
  updated: Date;

}
