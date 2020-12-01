import React, { memo, useEffect, useMemo } from 'react';
import { useAtom } from '@dbeining/react-atom';
import find from 'lodash/find';
import {
  usernameTextSharedState, initUserListData,
} from '@/components/UsernameText/sharedState';

interface OwnProps {
  userId?: string | number;
}

type Props = OwnProps;

/**
 * A component auto convert user id to name
 */
export const UsernameText: React.FC<Props> = memo((props) => {
  const { userId } = props;

  const {
    loading,
    error,
    userList,
  } = useAtom(usernameTextSharedState);

  useEffect(() => {
    initUserListData();
  }, [
  ]);

  const username = useMemo(() => {
    if (loading || error) {
      return '...';
    }
    if (userList && userList.length && userId) {
      const targetUser = find(userList, user => user.id === userId);
      if (targetUser) {
        return targetUser.username;
      }
      // else
      return '-';
    }
    return '...';
  }, [error, loading, userId, userList]);

  return (
    <span className="username-text" aria-label="user-name">
      {username}
    </span>
  );
});
