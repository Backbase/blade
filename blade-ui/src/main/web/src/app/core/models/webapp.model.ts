export interface WebApp {
  groupId: string;
  artifactId: string;
  version: string;
  isMavenModule: boolean;
  name: string;
  url: string;
  docBase: string;
  contextPath: string;
  contextFileLocation: string;
  startupTime: number;
  state: string;

}
