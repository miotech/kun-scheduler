import flatten from 'lodash/flatten';
import find from 'lodash/find';
import { Record as RTRecord, String, Array } from 'runtypes';
import {
  DatasetTaskDefSummary, RelatedTaskDefMetadata, TaskDatasetProperty
} from '@/definitions/DatasetTaskDefSummary.type';

const RelatedTaskDefMetadataType = RTRecord({
  id: String,
  name: String,
});

const DatasetTaskDefSummaryType = RTRecord({
  datastoreId: String,
  name: String,
  taskDefinitions: Array(RelatedTaskDefMetadataType),
});

/**
 * Extract dataset properties ({TaskDatasetProperty}) from a list of {DatasetTaskDefSummary} objects
 * @param datasets
 * @throws Error if shape of input argument does not conform
 */
export function getFlattenedTaskDefinition(datasets: DatasetTaskDefSummary[]): TaskDatasetProperty[] {
  // Precondition check, throws exception if datasets type not match
  Array(DatasetTaskDefSummaryType).check(datasets);

  return flatten(datasets.map(ds => ds.taskDefinitions.map(item => ({
    datasetName: ds.name,
    datastoreId: ds.datastoreId,
    definitionId: item.id,
  }))));
}

export function getTaskDefinitionsFromFlattenedProps(datasetProperties: TaskDatasetProperty[], upstreamTaskDefs: RelatedTaskDefMetadata[]): DatasetTaskDefSummary[] {
  const dsKeyToTaskDefs: Record<string, RelatedTaskDefMetadata[]> = {};

  datasetProperties.forEach(dsProp => {
    const key = `${dsProp.datastoreId}:::${dsProp.datasetName}`;
    if (!dsKeyToTaskDefs[key]) {
      dsKeyToTaskDefs[key] = [];
    }
    dsKeyToTaskDefs[key].push({
      id: dsProp.definitionId,
      name: find(upstreamTaskDefs, taskDef => taskDef.id === dsProp.definitionId)?.name || '',
    });
  });

  const ret: DatasetTaskDefSummary[] = [];
  Object.keys(dsKeyToTaskDefs).forEach(key => {
    const [ datastoreId, datasetName ] = key.split(':::');
    const pushItem: DatasetTaskDefSummary = {
      datastoreId,
      name: datasetName,
      taskDefinitions: [],
    };
    dsKeyToTaskDefs[key].forEach(taskDefMeta => {
      pushItem.taskDefinitions.push(taskDefMeta);
    });
    ret.push(pushItem);
  });

  return ret;
}
