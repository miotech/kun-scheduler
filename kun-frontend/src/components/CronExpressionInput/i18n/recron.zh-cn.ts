import { CronLocalization } from '@sbzen/re-cron';

export default {
  common: {
    month: {
      january: '一月',
      february: '二月',
      march: '三月',
      april: '四月',
      may: '五月',
      june: '六月',
      july: '七月',
      august: '八月',
      september: '九月',
      october: '十月',
      november: '十一月',
      december: '十二月',
    },
    dayOfWeek: {
      sunday: '周日',
      monday: '周一',
      tuesday: '周二',
      wednesday: '周三',
      thursday: '周四',
      friday: '周五',
      saturday: '周六',
    },
    dayOfMonth: {
      '1st': '第1个',
      '2nd': '第2个',
      '3rd': '第3个',
      '4th': '第4个',
      '5th': '第5个',
      '6th': '第6个',
      '7th': '第7个',
      '8th': '第8个',
      '9th': '第9个',
      '10th': '第10个',
      '11th': '第11个',
      '12th': '第12个',
      '13th': '第13个',
      '14th': '第14个',
      '15th': '第15个',
      '16th': '第16个',
      '17th': '第17个',
      '18th': '第18个',
      '19th': '第19个',
      '20th': '第20个',
      '21st': '第21个',
      '22nd': '第22个',
      '23rd': '第23个',
      '24th': '第24个',
      '25th': '第25个',
      '26th': '第26个',
      '27th': '第27个',
      '28th': '第28个',
      '29th': '第29个',
      '30th': '第30个',
      '31st': '第31个',
    },
  },
  tabs: {
    seconds: '秒',
    minutes: '分',
    hours: '时',
    day: '日',
    month: '月',
    year: '年',
  },
  quartz: {
    day: {
      every: { label: '每天' },
      dayOfWeekIncrement: { label1: '每隔', label2: '天，起始于' },
      dayOfMonthIncrement: { label1: '每隔', label2: '天，起始于每个月的', label3: '日' },
      dayOfWeekAnd: { label: '每周中的特定天（可多选）' },
      dayOfMonthAnd: { label: '每月中的特定日期（可多选）' },
      dayOfMonthLastDay: { label: '每月的最后一天' },
      dayOfMonthLastDayWeek: { label: '每月的最后一个工作日（周一至周五）' },
      dayOfWeekLastNTHDayWeek: {
        label1: '每月的最后一个',
        label2: '',
      },
      dayOfMonthDaysBeforeEndMonth: {
        label: '天于每月底前',
      },
      dayOfMonthNearestWeekDayOfMonth: {
        label1: '距离每月',
        label2: '日最近的一个工作日（周一至周五）',
      },
      dayOfWeekNTHWeekDayOfMonth: {
        label1: '每月的',
        label2: '',
      },
    },
    month: {
      every: {
        label: '每月',
      },
      increment: {
        label1: '每隔',
        label2: '个月，起始于',
      },
      and: {
        label: '特定月（可多选）',
      },
      range: {
        label1: '每个月，从',
        label2: '至',
      },
    },
    second: {
      every: {
        label: '每秒',
      },
      increment: {
        label1: '每隔',
        label2: '秒，起始于第 n 秒：',
      },
      and: {
        label: '特定秒（可多选）',
      },
      range: {
        label1: '在每分钟的第 n 至 m 秒：从',
        label2: '至',
      },
    },
    minute: {
      every: {
        label: '每分钟',
      },
      increment: {
        label1: '每隔',
        label2: '分钟，起始于第 n 分钟：',
      },
      and: {
        label: '特定分钟（可多选）',
      },
      range: {
        label1: '在每小时的第 n 分钟到 m 分钟：从',
        label2: '至',
      },
    },
    hour: {
      every: {
        label: '每小时',
      },
      increment: {
        label1: '每隔',
        label2: '个小时，起始于第 n 小时：',
      },
      and: {
        label: '特定小时（可多选）',
      },
      range: {
        label1: '在每天的第 n 小时到 m 小时：从',
        label2: '至',
      },
    },
    year: {
      every: {
        label: '每年',
      },
      increment: {
        label1: '每隔',
        label2: '年，起始于',
      },
      and: {
        label: '特定年（可多选）',
      },
      range: {
        label1: '从',
        label2: '至',
      },
    },
  },
} as CronLocalization;
