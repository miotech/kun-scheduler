declare module '*.css';
declare module '*.less';
declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.svg' {
  export function ReactComponent(
    props: React.SVGProps<SVGSVGElement>,
  ): React.ReactElement;
  const url: string;
  export default url;
}

interface Window {
  t?: (key: string, options?: any, defaultMsg?: string | undefined) => string;
  chrome: any;
  opr: any;
}

/* Usage Reference: https://www.npmjs.com/package/safe-url-assembler */
declare module 'safe-url-assembler' {
  export interface ParamReplacementMethodFn {
    (params: Record<string, any>, strict?: boolean): SafeUrlAssembler;
    (key: string, value: string, strict?: boolean): SafeUrlAssembler;
  }
  export interface QueryReplacementMethodFn {
    (params: Record<string, any>): SafeUrlAssembler;
    (key: string, value: string): SafeUrlAssembler;
  }
  export interface SafeUrlAssembler {
    (baseUrl?: string): SafeUrlAssembler;
    new (baseUrl?: string): SafeUrlAssembler;
    new (urlAssembler: SafeUrlAssembler): SafeUrlAssembler;
    param: ParamReplacementMethodFn;
    query: QueryReplacementMethodFn;
    prefix: (subPath: string) => SafeUrlAssembler;
    segment: (subPathTemplate: string) => SafeUrlAssembler;
    template: (urlTemplate: string) => SafeUrlAssembler;
    toString: () => string;
    toJSON: () => any;
    valueOf: () => any;
  }
  const SafeUrlAssembler: SafeUrlAssembler;
  export default SafeUrlAssembler;
}

declare module 'kndb' {
  export interface SetAction {
    errorInfo?: any;
  }
  export interface KnDBObject {
    success: boolean;
    get<T = any>(key: string): T;
    set<T = any>(key: string, value: T): SetAction;
  }
  export function getDB(
    name: string,
    options?: {
      type?: 'check';
      position?: string;
    },
  ): KnDBObject;
}

declare module 'flakeid' {
  export interface InitOptions {
    mid?: number;
    timeOffset?: number;
  }
  export interface FlakeIdModule {
    new (initOptions?: InitOptions): FlakeIdGenerator;
  }
  export interface FlakeIdGenerator {
    gen(): string;
  }
  const flakeIdModule: FlakeIdModule;
  export default flakeIdModule;
}
