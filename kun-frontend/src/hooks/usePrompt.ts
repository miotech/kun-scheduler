import { useEffect, useRef } from 'react';
import { useHistory } from 'react-router-dom';
import { UnregisterCallback } from 'history';

/**
 * Prompt conditionally when current page unloads(refresh, reload, etc.)
 * Can be applied on scenarios on doing refreshes with dirty form values.
 * @param when if true, show prompt when page is unloading
 * @param message message on the prompt alert
 */
export const usePrompt = (
  when: boolean,
  message: string = 'Are you sure you want to quit without saving your changes?',
) => {
  const history = useHistory();

  const self = useRef<UnregisterCallback | null>();

  const onWindowOrTabClose = (_event: BeforeUnloadEvent): string | undefined => {
    let event = _event;
    if (!when) {
      // eslint-disable-next-line
      return void 0;
    }
    if (typeof event === 'undefined') {
      event = window.event as BeforeUnloadEvent;
    }
    if (event) {
      event.returnValue = message;
    }
    return message;
  };

  useEffect(() => {
    if (when) {
      self.current = history.block(message);
    } else {
      self.current = null;
    }

    window.addEventListener('beforeunload', onWindowOrTabClose);

    return () => {
      if (self.current) {
        self.current();
        self.current = null;
      }

      window.removeEventListener('beforeunload', onWindowOrTabClose);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [message, when, onWindowOrTabClose]);
};
