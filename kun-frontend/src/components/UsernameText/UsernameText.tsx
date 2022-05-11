import React, { memo, useEffect, useMemo } from 'react';
import { useAtom } from '@dbeining/react-atom';
import find from 'lodash/find';
import { usernameTextSharedState, initUserListData } from '@/components/UsernameText/sharedState';

interface OwnProps {
  owner?: string;
}

type Props = OwnProps;

/**
 * A component auto convert user id to name
 */
export const UsernameText: React.FC<Props> = memo(props => {
  const { owner } = props;

  const { loading, error, userList } = useAtom(usernameTextSharedState);

  useEffect(() => {
    initUserListData();
  }, []);

  const username = useMemo(() => {
    if (loading || error) {
      return '...';
    }
    if (userList && userList.length && owner) {
      const targetUser = find(userList, user => user.username === owner);
      if (targetUser) {
        return targetUser.username;
      }
      // else
      return '-';
    }
    return '...';
  }, [error, loading, owner, userList]);

  return (
    <span className="username-text" aria-label="user-name">
      {username}
    </span>
  );
});
