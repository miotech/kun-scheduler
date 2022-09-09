import React, { useState, useCallback, Dispatch, SetStateAction } from 'react';
import { Input, Space } from 'antd';
import { UpOutlined, DownOutlined, CloseOutlined } from '@ant-design/icons';
import { Task } from '@/definitions/Gantt.type';
import { useUpdateEffect } from 'ahooks';
import _ from 'lodash';
import styles from './SearchTaskModal.less';

interface Props {
  infoList: Task[],
  setScrollTo: (index: number) => void,
  setSearchWords: (words: string) => void,
  setSearchModalVisible:  Dispatch<SetStateAction<boolean>>,
}

const SearchTaskModal: React.FC = (props: Props) => {
  const { infoList, setScrollTo, setSearchWords, setSearchModalVisible } = props;
  const [searchRes, setSearchRes] = useState({
    currentSearchIndex: 0,
    searchResult: []
  });

  const debounce = useCallback(
    _.debounce((_searchVal) => {
      if (!_searchVal) {
        setSearchWords('');
        setSearchRes({
          currentSearchIndex: 0,
          searchResult: []
        });
        return;
      }
      setSearchWords(_searchVal);
      const result = infoList?.filter((item) => {
        return item.name.toUpperCase().indexOf(_searchVal.toUpperCase()) !== -1;
      }) || [];
      const index = result.length > 0 ? 1 : 0;
      setSearchRes({
        currentSearchIndex: index,
        searchResult: result
      });
    }, 500),
    [infoList]
  );

  const handleChange = (event: any) => {
    debounce(event.target.value);
  };


  const handleNext = useCallback(() => {
    const { currentSearchIndex, searchResult } = searchRes;

    let index = currentSearchIndex + 1;
    if (searchResult.length === 0) {
      return;
    }
    if (currentSearchIndex === searchResult.length) {
      index = 1;
    }
    setSearchRes({
      currentSearchIndex: index,
      searchResult
    });
  }, [searchRes]);

  const handlePrev = useCallback(() => {
    const { currentSearchIndex, searchResult } = searchRes;

    let index = currentSearchIndex - 1;
    if (searchResult.length === 0) {
      return;
    }
    if (currentSearchIndex === 1) {
      index = searchResult.length;
    }
    setSearchRes({
      currentSearchIndex: index,
      searchResult
    });  
  }, [searchRes]);

  useUpdateEffect(() => {
    const { currentSearchIndex, searchResult } = searchRes;
    if (currentSearchIndex > 0) {
      const { taskRunId } = searchResult[currentSearchIndex - 1];
      const findIndex = infoList?.findIndex(item => item.taskRunId === taskRunId);

      setScrollTo(findIndex);
    }

  }, [searchRes]);

  const closeModal = () => {
    setSearchWords('');
    setSearchModalVisible(false);
  };
  
  return (
    <div className={styles.content}>
      <Space size="middle">
        <Input onChange={handleChange} bordered={false} />
        <div className={styles.num}>{`${searchRes?.currentSearchIndex}/${searchRes?.searchResult.length}`}</div>
        <div className={styles.border} />
        <UpOutlined onClick={handlePrev} />
        <DownOutlined onClick={handleNext} />
        <CloseOutlined onClick={closeModal}/>
      </Space>
    </div>
  );
};

export default SearchTaskModal;