---
slug: /docs/overview/concepts-zh
title: 概念
---

workflow 和 metadata 分别各有一些重要的概念。下面会分成两节介绍。

## Workflow

首先介绍 workflow 的概念。workflow 负责任务的调度与运行，最核心的概念就是 *Task* ，其他概念都是围绕 *Task* 而来。

### Task

Task 就是一个**需要定期执行的任务**，例如最常见的 ETL 任务。Task 一般会包括如下信息：

- 名称
- 任务内容
- 配置
- 调度周期
- 依赖关系

等。

和 *Task* 相关的另外还有两个重要的概念：*Operator* 和 *TaskRun*。

### Operator

Operator 是 Task 的载体，定义了 Task 实际执行的逻辑（以 jar 包的形式）。Operator 和 Task 的关系可以类比为类和实例的关系。Kun 内置有一些 Operator ，也允许用户自己上传自己的 Operator。

### TaskRun

TaskRun 表示的是 Task 的一次具体的运行。由于大部分 Task 都是周期运行的任务，其每一次运行都对应了一个 TaskRun。

### Others

*Task*，*Operator*，*TaskRun* 是 workflow 最核心的三个概念。在这之外还有 *Dependency*，*Config*，*Variable*，*TaskAttempt*等概念，以下一一介绍

#### Dependency

*Dependency* 是指任务的依赖关系。任务A 依赖 任务B 的含义是任务B 的 TaskRun 只有在任务A 的 TaskRun 运行结束之后才开始运行。

#### Config

*Config* 是指任务的所有配置信息，以 K-V 形式存储。可以认为 Task = Operator + Config 。

#### Variable

当有一系列 Task 的 *Config* 共享同一个信息（例如，db 的地址）时，可以在 *Config* 使用 *Variable* 来表示。*Variable* 表示的是一组共享的配置信息。

#### TaskAttempt

*TaskAttempt* 代表依次 TaskRun 的运行。因为 TaskRun 支持重试，每次重试都会产生一个 TaskAttempt，每个 TaskAttempt 都有独立的运行状态和日志。

## Metadata

除了 workflow，infra 层的另一个重要应用就是 metadata。metadata 负责元数据的收集，搜索和展示。

metadata 中最核心的概念是 *Dataset*。

### Dataset

*Dataset* 的含义是**一份存储在某种介质，符合某种格式的数据**。Dataset 主要包含两部分，*Schema* 是逻辑部分，即这份数据的格式。*DataStore* 是物理部分，即这份数据存储的介质信息。

#### Schema

*Schema* 是数据的结构，常见的类型有 Table，Document，Graph 等。针对每种类型，Schema 也包含更详细的信息，例如对于 Table 类型，Schema 里也包含字段名，字段类型，字段注释等信息。

#### DataStore

*DataStore* 是数据的物理存储信息，也分为很多种类型，常见的如 MySQL，Elasticsearch，neo4j 等。针对每种类型，各有不同的属性来存储更详细的信息。

### Lineage

#### Inlet/Outlet

*Inlet* 和 *Outlet* 分别是 Task 的输入和输出，主要用于构建任务的依赖关系以及数据的血缘图。Inlet 和 Outlet 的内容都是一个具体的 DataStore。
