/**
 * Copyright (c) 2019. Qubole Inc
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */
package com.qubole.rubix.spi;

/**
 * Created by sakshia on 27/9/16.
 */

import com.google.common.annotations.VisibleForTesting;
import com.qubole.rubix.spi.fop.Poolable;
import com.qubole.rubix.spi.thrift.BlockLocation;
import com.qubole.rubix.spi.thrift.BookKeeperService;
import com.qubole.rubix.spi.thrift.CacheStatusRequest;
import com.qubole.rubix.spi.thrift.ClusterNode;
import com.qubole.rubix.spi.thrift.FileInfo;
import com.qubole.rubix.spi.thrift.HeartbeatRequest;
import com.qubole.rubix.spi.thrift.ReadDataRequest;
import com.qubole.rubix.spi.thrift.SetCachedRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class RetryingPooledBookkeeperClient
        extends RetryingPooledThriftClient
        implements BookKeeperService.Iface
{
  private static final Log log = LogFactory.getLog(RetryingPooledBookkeeperClient.class);

  @VisibleForTesting
  public RetryingPooledBookkeeperClient()
  {
    super(1, null, null, null);
  }

  public RetryingPooledBookkeeperClient(Poolable<TTransport> transportPoolable, String host, Configuration conf)
  {
    super(CacheConfig.getMaxRetries(conf), conf, host, transportPoolable);
  }

  public TServiceClient setupClient(Poolable<TTransport> transportPoolable)
  {
    return new BookKeeperService.Client(new TBinaryProtocol(transportPoolable.getObject()));
  }

  private BookKeeperService.Client client()
  {
    return (BookKeeperService.Client) client;
  }

  @Override
  public List<BlockLocation> getCacheStatus(final CacheStatusRequest request) throws TException
  {
    return retryConnection(new Callable<List<BlockLocation>>()
    {
      @Override
      public List<BlockLocation> call()
          throws TException
      {
        return client().getCacheStatus(request);
      }
    });
  }

  @Override
  public void setAllCached(final SetCachedRequest request) throws TException
  {
    retryConnection(new Callable<Void>()
    {
      @Override
      public Void call()
          throws Exception
      {
        client().setAllCached(request);
        return null;
      }
    });
  }

  @Override
  public Map<String, Double> getCacheMetrics()
          throws TException
  {
    return retryConnection(new Callable<Map<String, Double>>()
    {
      @Override
      public Map<String, Double> call()
              throws TException
      {
        return client().getCacheMetrics();
      }
    });
  }

  @Override
  public boolean readData(final ReadDataRequest request)
          throws TException
  {
    return retryConnection(new Callable<Boolean>()
    {
      @Override
      public Boolean call()
              throws TException
      {
        return client().readData(request);
      }
    });
  }

  @Override
  public void handleHeartbeat(final HeartbeatRequest request) throws TException
  {
    retryConnection(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        client().handleHeartbeat(request);
        return null;
      }
    });
  }

  @Override
  public FileInfo getFileInfo(final String remotePath)
          throws TException
  {
    return retryConnection(new Callable<FileInfo>()
    {
      @Override
      public FileInfo call()
              throws TException
      {
        return client().getFileInfo(remotePath);
      }
    });
  }

  @Override
  public List<ClusterNode> getClusterNodes()
          throws TException
  {
    return retryConnection(new Callable<List<ClusterNode>>()
    {
      @Override
      public List<ClusterNode> call()
              throws TException
      {
        return client().getClusterNodes();
      }
    });
  }

  @Override
  public String getOwnerNodeForPath(final String remotePathKey)
          throws TException
  {
    return retryConnection(new Callable<String>()
    {
      @Override
      public String call()
              throws TException
      {
        return client().getOwnerNodeForPath(remotePathKey);
      }
    });
  }

  @Override
  public boolean isBookKeeperAlive()
          throws TException
  {
    return retryConnection(new Callable<Boolean>()
    {
      @Override
      public Boolean call()
              throws TException
      {
        return client().isBookKeeperAlive();
      }
    });
  }

  @Override
  public void invalidateFileMetadata(final String remotePath)
          throws TException
  {
    retryConnection(new Callable<Void>()
    {
      @Override
      public Void call()
              throws TException
      {
        client().invalidateFileMetadata(remotePath);
        return null;
      }
    });
  }
}
