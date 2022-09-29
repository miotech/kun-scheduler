export enum ResourceAttribute {
  type = 'type',
  datasource = 'datasource',
  database = 'database',
  schema = 'schema',
  tags = 'tags',
  owners = 'owners',
  glossaries = 'glossaries',
}
export interface QueryAttributeListParams {
  resourceAttributeName: string;
  resourceAttributeMap: {
    [key in ResourceAttribute]?: string | null | Record<string, unknown>[];
  };
}
