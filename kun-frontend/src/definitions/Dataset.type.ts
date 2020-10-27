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
}

export interface Watermark {
  user: string;
  time: number;
}

export interface GlossaryItem {
  id: string;
  name: string;
}
