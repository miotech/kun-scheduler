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
}

/* Usage Reference: https://www.npmjs.com/package/safe-url-assembler */
declare module 'safe-url-assembler' {
  export interface ParamReplacementMethodFn {
    (params: Record<string, any>, strict?: boolean): ISafeUrlAssembler;
    (key: string, value: string, strict?: boolean): ISafeUrlAssembler;
  }
  export interface QueryReplacementMethodFn {
    (params: Record<string, any>): ISafeUrlAssembler;
    (key: string, value: string): ISafeUrlAssembler;
  }
  export interface ISafeUrlAssembler {
    (baseUrl?: string): ISafeUrlAssembler;
    new (baseUrl?: string): ISafeUrlAssembler;
    new (urlAssembler: ISafeUrlAssembler): ISafeUrlAssembler;
    param: ParamReplacementMethodFn;
    query: QueryReplacementMethodFn;
    prefix: (subPath: string) => ISafeUrlAssembler;
    segment: (subPathTemplate: string) => ISafeUrlAssembler;
    template: (urlTemplate: string) => ISafeUrlAssembler;
    toString: () => string;
    toJSON: () => any;
    valueOf: () => any;
  }
  const SafeUrlAssembler: ISafeUrlAssembler;
  export default SafeUrlAssembler;
}
