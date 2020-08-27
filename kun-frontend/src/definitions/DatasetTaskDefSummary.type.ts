export interface DatasetTaskDefSummary {
  datastoreId: string;
  name: string;
  taskDefinitions: RelatedTaskDefMetadata[];
}

export interface RelatedTaskDefMetadata {
  id: string;
  name: string;
}

export interface TaskDatasetProperty {
  datasetName: string;
  datastoreId: string;
  definitionId: string;
}
