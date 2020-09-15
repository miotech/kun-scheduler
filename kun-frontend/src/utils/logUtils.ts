import debug, { Debugger } from 'debug';

declare const LOG_LEVEL: string | number;

const logLevel: Record<string, number> = {
  trace: 1,
  debug: 2,
  info: 3,
  warn: 4,
  error: 5,
  fatal: 6,
};

export interface LoggerExtraConfig {
  color?: string;
}

export default class LogUtils {
  protected static rootLogger = debug('kun');

  protected static initialized = false;

  static initialize() {
    if (LOG_LEVEL && window.localStorage) {
      let minLevel = 3;
      if (typeof LOG_LEVEL === 'string') {
        minLevel = logLevel[LOG_LEVEL] ?? 3;
      } else {
        minLevel = LOG_LEVEL;
      }
      debug.disable();
      /* Filter out low level loggers */
      Object.keys(logLevel).forEach(logLevelKey => {
        if (logLevel[logLevelKey] >= minLevel) {
          const activatedNamespaces = debug.disable();
          debug.enable(`${activatedNamespaces},kun:${logLevelKey}:*`);
        }
      });
    }
    this.initialized = true;
  }

  public static log(...args: any[]) {
    LogUtils.log(args);
  }

  public static getLoggers(namespace: string) {
    return {
      trace: LogUtils.getTraceLogger(namespace),
      debug: LogUtils.getDebugLogger(namespace),
      info: LogUtils.getInfoLogger(namespace),
      warn: LogUtils.getWarnLogger(namespace),
      error: LogUtils.getErrorLogger(namespace),
      fatal: LogUtils.getFatalLogger(namespace),
    };
  }

  public static getTraceLogger(namespace: string, extraConfig?: LoggerExtraConfig): Debugger {
    if (!LogUtils.initialized) {
      this.initialize();
    }
    const logger = debug(`kun:trace:${namespace}`);
    if (extraConfig && extraConfig.color) {
      logger.color = extraConfig.color;
    } else {
      logger.color = '#cdcdcd';
    }
    return logger;
  }

  public static getDebugLogger(namespace: string, extraConfig?: LoggerExtraConfig): Debugger {
    if (!LogUtils.initialized) {
      this.initialize();
    }
    const logger = debug(`kun:debug:${namespace}`);
    if (extraConfig && extraConfig.color) {
      logger.color = extraConfig.color;
    } else {
      logger.color = '#b833b8';
    }
    return logger;
  }

  public static getInfoLogger(namespace: string, extraConfig?: LoggerExtraConfig): Debugger {
    if (!LogUtils.initialized) {
      this.initialize();
    }
    const logger = debug(`kun:info:${namespace}`);
    if (extraConfig && extraConfig.color) {
      logger.color = extraConfig.color;
    } else {
      logger.color = '#96c2ee';
    }
    return logger;
  }

  public static getWarnLogger(namespace: string, extraConfig?: LoggerExtraConfig): Debugger {
    if (!LogUtils.initialized) {
      this.initialize();
    }
    const logger = debug(`kun:warn:${namespace}`);
    logger.log = console.warn.bind(console);
    if (extraConfig && extraConfig.color) {
      logger.color = extraConfig.color;
    } else {
      logger.color = '#d7a330';
    }
    return logger;
  }

  public static getErrorLogger(namespace: string, extraConfig?: LoggerExtraConfig): Debugger {
    if (!LogUtils.initialized) {
      this.initialize();
    }
    const logger = debug(`kun:error:${namespace}`);
    logger.log = console.error.bind(console);
    if (extraConfig && extraConfig.color) {
      logger.color = extraConfig.color;
    } else {
      logger.color = '#d53200';
    }
    return logger;
  }

  public static getFatalLogger(namespace: string, extraConfig?: LoggerExtraConfig): Debugger {
    if (!LogUtils.initialized) {
      this.initialize();
    }
    const logger = debug(`kun:fatal:${namespace}`);
    logger.log = console.error.bind(console);
    if (extraConfig && extraConfig.color) {
      logger.color = extraConfig.color;
    } else {
      logger.color = '#ff3b00';
    }
    return logger;
  }
}
