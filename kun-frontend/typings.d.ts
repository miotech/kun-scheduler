/* eslint-disable */
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

declare module '*.worker.ts' {
  class WebpackWorker extends Worker {
    constructor();
  }

  export default WebpackWorker;
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


declare module 'kld-affine' {
  class Point2D {
    x: number
    y: number

    constructor (x: number, y: number)

    clone (): Point2D

    add (that: Point2D): Point2D

    subtract (that: Point2D): Point2D

    multiply (scalar: number): Point2D

    divide (scalar: number): Point2D

    equals (that: Point2D): boolean

    precisionEquals (that: Point2D, precision: number): boolean

    lerp (that: Point2D, t: number): Point2D

    distanceFrom (that: Point2D): number

    min (that: Point2D): Point2D

    max (that: Point2D): Point2D

    transform (matrix: Matrix2D): Point2D

    toString (): string
  }

  class Vector2D {
    x: number
    y: number

    constructor (x: number, y: number)

    static fromPoints(p1: Point2D, p2: Point2D): Vector2D

    length (): number

    magnitude (): number

    dot (that: Vector2D): number

    cross (that: Vector2D): number

    determinant (that: Vector2D): number

    unit (): Vector2D

    add (that: Vector2D): Vector2D

    subtract (that: Vector2D): Vector2D

    multiply (scalar: number): Vector2D

    divide (scalar: number): Vector2D

    angleBetween (that: Vector2D): number

    perp (): Vector2D

    perpendicular (that: Vector2D): Vector2D

    project (that: Vector2D): Vector2D

    transform (matrix: Matrix2D): Vector2D

    equals (that: Vector2D): boolean

    precisionEquals(that: Vector2D, precision: number): boolean

    toString (): string
  }

  class Matrix2D {
    a: number
    b: number
    c: number
    d: number
    e: number
    f: number

    constructor (
      a: number,
      b: number,
      c: number,
      d: number,
      e: number,
      f: number
    )

    static IDENTITY: Matrix2D

    static translation (tx: number, ty: number): Matrix2D

    static scaling (scale: number): Matrix2D

    static scalingAt (scale: number, center: Point2D): Matrix2D

    static nonUniformScaling (scaleX: number, scaleY: number): Matrix2D

    static nonUniformScalingAt (scaleX: number, scalyY: number, center: Point2D): Matrix2D

    static rotation (radians: number): Matrix2D

    static rotationAt (radians: number, center: Point2D): Matrix2D

    static rotationFromVector (vector: Vector2D): Matrix2D

    static xFlip (): Matrix2D

    static yFlip (): Matrix2D

    static xSkew (radians: number): Matrix2D

    static ySkew (radians: number): Matrix2D

    multiply (that: Matrix2D): Matrix2D

    inverse (): Matrix2D

    translate (tx: number, ty: number): Matrix2D

    scale (scale: number): Matrix2D

    scaleAt (scale: number, center: Point2D): Matrix2D

    scaleNonUniform (scaleX: number, scaleY: number): Matrix2D

    scaleNonUniformAt (scaleX: number, scaleY: number, center: Point2D): Matrix2D

    rotate (radians: number): Matrix2D

    rotateAt (radians: number, center: Point2D): Matrix2D

    rotateFromVector (vector: number): Matrix2D

    flipX (): Matrix2D

    flipY (): Matrix2D

    skewX (radians: number): Matrix2D

    skewY (radians: number): Matrix2D

    isIndentity (): boolean

    isInvertible (): boolean

    getScale (): { scaleX: number, scaleY: number }

    getDecomposition (): { translation: Matrix2D, rotation: Matrix2D, scale: Matrix2D, rotation0: Matrix2D }

    equals (that: Matrix2D): boolean

    precisionEquals (that: Matrix2D, precision: number): boolean

    toString (): string
  }
}


declare module 'kld-intersections' {
  import { Point2D } from 'kld-affine';

  export class ShapeInfo {
    public static ARC: ShapeName.ARC;

    public static QUADRATIC_BEZIER: ShapeName.QUADRATIC_BEZIER;

    public static CUBIC_BEZIER: ShapeName.CUBIC_BEZIER;

    public static CIRCLE: ShapeName.CIRCLE;

    public static ELLIPSE: ShapeName.ELLIPSE;

    public static LINE: ShapeName.LINE;

    public static PATH: ShapeName.PATH;

    public static POLYGON: ShapeName.POLYGON;

    public static RECTANGLE: ShapeName.RECTANGLE;

    public static getValues(types: any[], args: any): any[];
    public static cubicBezier(...args: any[]): ShapeInfo;
    public static circle(...args: any[]): ShapeInfo;
    public static ellipse(...args: any[]): ShapeInfo;
    public static line(args: any): ShapeInfo;
    public static path(...args: any[]): ShapeInfo;
    public static polygon(...args: any[]): ShapeInfo;
    public static polyline(...args: any[]): ShapeInfo;
    public static rectangle(...args: any[]): ShapeInfo;
  }

  export class Intersection {
    constructor(status: string);
    static intersect(shape1: ShapeInfo, shape2: ShapeInfo): Intersection;
    static intersectPathShape(path: ShapeInfo, shape: ShapeInfo): Intersection;
    static intersectArcShape(arc: ShapeInfo, shape: ShapeInfo): Intersection;
    static intersectBezier2Bezier2(a1: Point2D, a2: Point2D, a3: Point2D, b1: Point2D, b2: Point2D, b3: Point2D): Intersection;
    static intersectBezier2Bezier3(a1: Point2D, a2: Point2D, a3: Point2D, b1: Point2D, b2: Point2D, b3: Point2D, b4: Point2D): Intersection;
    static intersectBezier2Circle(p1: Point2D, p2: Point2D, p3: Point2D, c: Point2D, r: number): Intersection;
    static intersectBezier2Ellipse(p1: Point2D, p2: Point2D, p3: Point2D, ec: Point2D, rx: number, ry: number): Intersection;
    static intersectBezier2Line(p1: Point2D, p2: Point2D, p3: Point2D, a1: Point2D, a2: Point2D): Intersection;
    static intersectBezier2Polygon(p1: Point2D, p2: Point2D, p3: Point2D, points: Point2D[]): Intersection;
    static intersectBezier2Polyline(p1: Point2D, p2: Point2D, p3: Point2D, points: Point2D[]): Intersection;
    static intersectBezier2Rectangle(p1: Point2D, p2: Point2D, p3: Point2D, r1: Point2D, r2: Point2D): Intersection;
    static intersectBezier3Bezier3(a1: Point2D, a2: Point2D, a3: Point2D, a4: Point2D, b1: Point2D, b2: Point2D, b3: Point2D, b4: Point2D): Intersection;
    static intersectBezier3Circle(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, c: Point2D, r: number): Intersection;
    static intersectBezier3Ellipse(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, ec: Point2D, rx: number, ry: number): Intersection;
    static intersectBezier3Line(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, a1: Point2D, a2: Point2D): Intersection;
    static intersectBezier3Polygon(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, points: Point2D[]): Intersection;
    static intersectBezier3Polyline(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, points: Point2D[]): Intersection;
    static intersectBezier3Rectangle(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D, r1: Point2D, r2: Point2D): Intersection;
    static intersectCircleCircle(c1: Point2D, r1: number, c2: any, r2: number): Intersection;
    static intersectCircleEllipse(cc: Point2D, r: number, ec: Point2D, rx: number, ry: number): Intersection;
    static intersectCircleLine(c: Point2D, r: number, a1: Point2D, a2: Point2D): Intersection;
    static intersectCirclePolygon(c: Point2D, r: number, points: Point2D[]): Intersection;
    static intersectCirclePolyline(c: Point2D, r: number, points: Point2D[]): Intersection;
    static intersectCircleRectangle(c: Point2D, r: number, r1: Point2D, r2: Point2D): Intersection;
    static intersectEllipseEllipse(c1: Point2D, rx1: number, ry1: number, c2: Point2D, rx2: number, ry2: number): Intersection;
    static intersectEllipseLine(c: Point2D, rx: number, ry: number, a1: Point2D, a2: Point2D): Intersection;
    static intersectEllipsePolygon(c: Point2D, rx: number, ry: number, points: Point2D[]): Intersection;
    static intersectEllipsePolyline(c: Point2D, rx: number, ry: number, points: Point2D[]): Intersection;
    static intersectEllipseRectangle(c: Point2D, rx: number, ry: number, r1: Point2D, r2: Point2D): Intersection;
    static intersectLineLine(a1: Point2D, a2: Point2D, b1: Point2D, b2: Point2D[]): Intersection;
    static intersectLinePolygon(a1: Point2D, a2: Point2D, points: Point2D[]): Intersection;
    static intersectLinePolyline(a1: Point2D, a2: Point2D, points: Point2D[]): Intersection;
    static intersectLinePolyline(a1: Point2D, a2: Point2D, points: Point2D[]): Intersection;
    static intersectLineRectangle(a1: Point2D, a2: Point2D, r1: Point2D, r2: Point2D): Intersection;
    static intersectPolygonPolygon(points1: Point2D, points2: Point2D): Intersection;
    static intersectPolygonPolyline(points1: Point2D, points2: Point2D): Intersection;
    static intersectPolygonRectangle(points: Point2D[], r1: Point2D, r2: Point2D): Intersection;
    static intersectPolylinePolyline(points1: Point2D, points2: Point2D): Intersection;
    static intersectPolylineRectangle(points: Point2D[], r1: Point2D, r2: Point2D): Intersection;
    static intersectRectangleRectangle(a1: Point2D, a2: Point2D, b1: Point2D, b2: Point2D): Intersection;
    static intersectRayRay(a1: Point2D, a2: Point2D, b1: Point2D, b2: Point2D): Intersection;
    status: string;
    points: Point2D[];
    appendPoint(point: Point2D): void;
    appendPoints(points: Point2D[]): void;
  }

  enum ShapeName {
    ARC = "Arc",
    QUADRATIC_BEZIER = "Bezier2",
    CUBIC_BEZIER = "Bezier3",
    CIRCLE = "Circle",
    ELLIPSE = "Ellipse",
    LINE = "Line",
    PATH = "Path",
    POLYGON = "Polygon",
    POLYLINE = "Polyline",
    RECTANGLE = "Rectangle",
  }

  export namespace IntersectionQuery {
    export function pointInCircle(point: Point2D, center: Point2D, radius: number): boolean;
    export function pointInEllipse(point: Point2D, center: Point2D, radiusX: number, radiusY: number): boolean;
    export function pointInPolyline(point: Point2D, points: Point2D[]): boolean;
    export const pointInPolygon: Point2D;
    export function pointInRectangle(point: Point2D, topLeft: Point2D, bottomRight: Point2D): boolean;
  }
}
