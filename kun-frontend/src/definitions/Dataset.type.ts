export interface Dataset {
  id: string;
  name: string;
  schema: string;
  description: string;
  type: string;
  datasource: string;
  database: string;
  highWatermark: Watermark;
  owners: string[];
  tags: string[];
  glossaries: GlossaryItem[];
  deleted: boolean;
}

export interface Watermark {
  user: string;
  time: number;
}

export interface GlossaryItem {
  id: string;
  name: string;
}

export interface Edge {
  sourceVertexId: string;
  destVertexId: string;
}

export interface DatasetBasic {
  gid: string;
  name: string;
  datasource: string;
  type: string;
  rowCount: number;
  highWatermark: Watermark;
}

export interface Vertex {
  vertexId: string;
  datasetBasic: DatasetBasic;
  upstreamVertexCount: number;
  downstreamVertexCount: number;
}
