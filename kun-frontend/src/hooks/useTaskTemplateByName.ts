import { useEffect, useState } from 'react';
import find from 'lodash/find';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { fetchTaskTemplates } from '@/services/data-development/task-templates';
import LogUtils from '@/utils/logUtils';

const logger = LogUtils.getLoggers('hook:useTaskTemplateByName');

export function useTaskTemplateByName(taskTemplateName?: string): [TaskTemplate | null, boolean] {
  const [ taskTemplate, setTaskTemplate ] = useState<TaskTemplate | null>(null);
  const [ isLoading, setIsLoading ] = useState<boolean>(false);

  useEffect(() => {
    if (taskTemplateName && taskTemplateName.length) {
      setIsLoading(true);
      fetchTaskTemplates()
        .then((templates) => {
          const targetTemplate = find(templates || [], template => template.name === taskTemplateName);
          setTaskTemplate(targetTemplate || null);
        })
        .catch((e) => {
          logger.error(e);
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, [
    taskTemplateName,
    setTaskTemplate,
  ]);

  return [taskTemplate, isLoading];
}
